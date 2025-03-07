package haru.harudrawer.domain.auth.service;

import haru.harudrawer.domain.auth.controller.dto.request.CommonRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;
import haru.harudrawer.domain.auth.controller.dto.response.LocalResponseDTO;
import haru.harudrawer.domain.auth.entity.User;
import haru.harudrawer.domain.auth.exception.AuthErrorCode;
import haru.harudrawer.domain.auth.exception.AuthException;
import haru.harudrawer.domain.auth.repository.AuthRepository;
import haru.harudrawer.domain.diary.repository.DiaryRepository;
import haru.harudrawer.global.redis.RedisService;
import haru.harudrawer.global.s3.S3Service;
import haru.harudrawer.global.security.domain.CustomUserDetails;
import haru.harudrawer.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommonAuthService {

    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;


    // Authorization 헤더에서 실제 JWT 토큰 문자열만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 사용자 로그 아웃
     */
    public void logout(HttpServletRequest request) {
        String token = resolveToken(request);

        // 토큰 유효성 검사
        if (!jwtProvider.validateToken(token)) {
            throw new AuthException(AuthErrorCode.ALREADY_LOGOUT_USER);
        }

        redisService.deleteRefreshToken(jwtProvider.getUserEmail(token));

        // 토큰 블랙리스트에 추가
        jwtProvider.addTokenToBlackList(token);
    }

    /**
     * 토큰 검증
     */
    public void validateToken(HttpServletRequest request) {
        String token = resolveToken(request);

        if (!jwtProvider.validateToken(token)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }


    /**
     * 사용자 정보 조회
     */
    public CommonResponseDTO.GetUserInfoDTO getUserInfo(HttpServletRequest request) {

        // 토큰 검증
        validateToken(request);

        // 토큰을 통해 사용자 이메일 조회
        String userEmail = jwtProvider.getUserEmail(resolveToken(request));

        User findUser = authRepository.findByUserEmail(userEmail).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        return CommonResponseDTO.GetUserInfoDTO.builder()
                .nickName(findUser.getNickName())
                .userEmail(findUser.getUserEmail())
                .provider(findUser.getProvider())
                .build();
    }

    /**
     * 사용자 이메일 수정
     */
    public LocalResponseDTO.LocalLoginResponseDTO updateUserEmail(CommonRequestDTO.UpdateEmailDTO request) {

        User user = authRepository.findByUserId(jwtProvider.extractUserId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        user.updateEmail(request.getUserEmail());

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .userId(user.getUserId())
                .email(request.getUserEmail())
                .password(user.getPassword())
                .provider(user.getProvider())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())))
                .build();

        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(request.getUserEmail());

        return LocalResponseDTO.LocalLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    /**
     * 사용자 닉네임 수정
     */
    public void updateUserNickName(CommonRequestDTO.UpdateNickNameDTO request) {

        User user = authRepository.findByUserId(jwtProvider.extractUserId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 이전 닉네임과 동일하지 않을경우 수정
        if (!user.getNickName().equals(request.getNickName())) {
            user.updateNickName(request.getNickName());
        }
    }

    /**
     * 사용자 비밀번호 수정
     */
    public void updateUserPassword(CommonRequestDTO.UpdatePasswordDTO request) {

        User user = authRepository.findByUserId(jwtProvider.extractUserId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        //비밀번호 일치할 경우
        if (passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {

            // 변경 비밀번호, 변경 비밀번호 확인 서로 다를경우
            if (!request.getNewPassword().equals(request.getNewPasswordCheck())) {
                throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
            }

            // 비밀번호 서식 틀렸을 경우 예외처리
            if (!request.getNewPassword().matches("^(?=.*[A-Z])(?=.*[@$!%*?&]).{8,16}$")
                    || !request.getNewPasswordCheck().matches("^(?=.*[A-Z])(?=.*[@$!%*?&]).{8,16}$") ) {
                throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
            }

            // 변경 전 비밀번호와 같을 경우 예외
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new AuthException(AuthErrorCode.DUPLICATE_PASSWORD);
            }

            // 비밀번호 업데이트
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    /**
     * refresh Token으로 Access Token 재발급
     */
    public LocalResponseDTO.LocalLoginResponseDTO refreshToken(CommonRequestDTO.TokenRefreshDTO request) {
        // 사용자 이메일 조회
        String userEmail = jwtProvider.getUserEmail(request.getRefreshToken());

        // redis에서 refresh token 조회
        Optional<String> findTokenOpt = redisService.getRefreshToken(userEmail);

        // refresh token 검증
        if (findTokenOpt.isEmpty() || !findTokenOpt.get().equals(request.getRefreshToken())) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        // 이메일로 사용자 정보 DB 조회
        User user = authRepository.findByUserEmail(userEmail).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 유저 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getUserId(),
                user.getUserEmail(),
                null,
                user.getNickName(),
                user.getProvider(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );

        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getEmail());

        // redis에서 사용된 refresh token삭제
        redisService.deleteRefreshToken(userEmail);

        // 새로운 refresh token 저장
        redisService.saveRefreshToken(userEmail, refreshToken);

        return LocalResponseDTO.LocalLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }



}
