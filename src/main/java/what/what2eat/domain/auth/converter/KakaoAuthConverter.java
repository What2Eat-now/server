package what.what2eat.domain.auth.converter;

import org.springframework.stereotype.Component;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.controller.dto.KakaoAuthRequestDTO;

@Component
public class KakaoAuthConverter {

    public User signupToUserEntity(KakaoAuthRequestDTO.KakaoSignupDTO request) {
        return User.builder()
                .nickName(request.getNickName())
                .userEmail(request.getUserEmail())
                .role(Role.USER)
                .provider(Provider.KAKAO)
                .socialId(request.getSocialId())
                .build();
    }

}
