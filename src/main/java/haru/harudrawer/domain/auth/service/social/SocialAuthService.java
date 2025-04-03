package haru.harudrawer.domain.auth.service.social;

import haru.harudrawer.domain.auth.controller.dto.response.CommonResponseDTO;

public interface SocialAuthService {
    CommonResponseDTO.LoginResponseDTO login(String tokenOrCode) throws Exception;

    void delete() throws Exception;
}
