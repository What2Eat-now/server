package what.what2eat.domain.auth.converter;

import org.springframework.stereotype.Component;
import what.what2eat.domain.auth.controller.dto.AuthRequestDTO;
import what.what2eat.domain.auth.entity.Provider;
import what.what2eat.domain.auth.entity.Role;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.entity.UserStatus;

@Component
public class KakaoAuthConverter {

    public User signupToUserEntity(AuthRequestDTO.KakaoSignupDTO request) {
        return User.builder()
                .nickName(request.getNickName())
                .userEmail(request.getUserEmail())
                .role(Role.USER)
                .provider(Provider.KAKAO)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

}
