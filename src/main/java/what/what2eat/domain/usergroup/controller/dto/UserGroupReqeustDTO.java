package what.what2eat.domain.usergroup.controller.dto;

import lombok.Builder;
import lombok.Getter;

public class UserGroupReqeustDTO {

    @Builder
    @Getter
    public static class CreateGroupDTO {

        private String groupName;

        private String userEmail;
    }
}
