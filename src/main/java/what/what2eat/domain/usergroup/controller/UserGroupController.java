package what.what2eat.domain.usergroup.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<ApiResponse<Void>> createGroup(
            @RequestBody UserGroupReqeustDTO.CreateGroupDTO request) throws Exception {
        userGroupService.createGroup(request);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
