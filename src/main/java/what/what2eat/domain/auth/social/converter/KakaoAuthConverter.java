package what.what2eat.domain.auth.social.converter;

import org.springframework.stereotype.Component;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.social.controller.dto.KakaoAuthResponseDTO;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.User;

@Component
public class KakaoAuthConverter {

    public User kakaoToUserEntity(KakaoAuthResponseDTO.KakaoUserInfoDTO userInfoDTO) {
        return User.builder()
                .userEmail(userInfoDTO.getKakaoAccount().getKakaoEmail())
                .userImg(userInfoDTO.getProperties().get("profile_image"))
                .nickName(userInfoDTO.getProperties().get("nickname"))
                .provider(Provider.KAKAO)
                .socialId(userInfoDTO.getUserId())
                .role(Role.USER)
                .build();
    }

}
