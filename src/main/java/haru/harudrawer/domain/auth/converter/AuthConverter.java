package haru.harudrawer.domain.auth.converter;

import haru.harudrawer.domain.auth.controller.dto.request.SocialRequestDTO;
import haru.harudrawer.domain.auth.entity.Provider;
import haru.harudrawer.domain.auth.entity.Role;
import haru.harudrawer.domain.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthConverter {

    public User userEmailToSocialUserEntity(SocialRequestDTO.SocialUserInfoDTO userInfo, Provider provider) {
        if (provider.equals(Provider.APPLE)) {
            return User.builder()
                    .nickName("이름을 변경해주세요.")
                    .userEmail(userInfo.getUserEmail())
                    .role(Role.USER)
                    .provider(Provider.APPLE)
                    .build();
        } else if (provider.equals(Provider.KAKAO)) {
            return User.builder()
                    .nickName(userInfo.getNickName())
                    .userEmail(userInfo.getUserEmail())
                    .role(Role.USER)
                    .provider(Provider.KAKAO)
                    .build();}
        return null;
    }
}
