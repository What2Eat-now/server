package haru.harudrawer.domain.auth.converter;

import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.controller.dto.response.SocialResponseDTO;
import org.springframework.stereotype.Component;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.Role;
import haru.harudrawer.domain.auth.entity.User;

@Component
public class AuthConverter {

    public User signupToKakaoUserEntity(SocialResponseDTO.KakaoUserInfoDTO request) {
        return User.builder()
                .nickName(request.getKakaoAccount().getProfile().getKakaoNickName())
                .userEmail(request.getKakaoAccount().getKakaoEmail())
                .role(Role.USER)
                .provider(Provider.KAKAO)
                .build();
    }

    public User userEmailToAppleUserEntity(String userEmail) {
        return User.builder()
                .nickName("이름을 변경해주세요")
                .userEmail(userEmail)
                .role(Role.USER)
                .provider(Provider.APPLE)
                .build();
    }

}
