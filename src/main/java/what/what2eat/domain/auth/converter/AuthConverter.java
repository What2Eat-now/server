package what.what2eat.domain.auth.converter;

import org.springframework.stereotype.Component;
import what.what2eat.domain.auth.controller.dto.AuthResponseDTO;
import what.what2eat.domain.auth.entity.SocialType;
import what.what2eat.domain.auth.entity.User;

@Component
public class AuthConverter {

    public User kakaoToUserEntity(AuthResponseDTO.KakaoUserInfoDTO userInfoDTO) {
        return User.builder()
                .userEmail(userInfoDTO.getKakaoAccount().getKakaoEmail())
                .userImg(userInfoDTO.getProperties().get("profile_image"))
                .nickName(userInfoDTO.getProperties().get("nickname"))
                .provider(SocialType.KAKAO)
                .socialId(userInfoDTO.getUserId())
                .build();
    }
}
