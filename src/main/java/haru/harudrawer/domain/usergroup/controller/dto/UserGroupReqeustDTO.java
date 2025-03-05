package haru.harudrawer.domain.usergroup.controller.dto;

import lombok.Builder;
import lombok.Getter;

public class UserGroupReqeustDTO {

    @Builder
    @Getter
    public static class CreateUserGroupDTO {

        private String groupName;

        private String userEmail;

        private String groupPassword;
    }

    @Builder
    @Getter
    public static class JoinUserGroupDTO {

        private String userEmail;

        private String groupPassword;
    }
}
