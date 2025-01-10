package what.what2eat.domain.usergroup.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import what.what2eat.domain.usergroup.controller.dto.UserGroupReqeustDTO;
import what.what2eat.domain.usergroup.service.UserGroupService;
import what.what2eat.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/groups")
public class UserGroupController {

    private final UserGroupService userGroupService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> createUserGroup(
            @RequestBody UserGroupReqeustDTO.CreateUserGroupDTO request) {
        userGroupService.createUserGroup(request);

        return ResponseEntity.ok(ApiResponse.of("그룹 생성 완료."));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<String>> joinUserGroup(@PathVariable Long groupId,
                                                         @RequestBody UserGroupReqeustDTO.JoinUserGroupDTO request) {
        userGroupService.joinUserGroup(groupId, request);

        return ResponseEntity.ok(ApiResponse.of("그룹 참가 완료."));
    }

    @PostMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse<String>> withdrawUserGroup(@PathVariable Long groupId,
                                                             @PathVariable("memberId") Long userId) {

        userGroupService.withDrawUserGroup(groupId, userId);

        return ResponseEntity.ok(ApiResponse.of("그룹 탈퇴가 완료되었습니다."));
    }
}
