package what.what2eat.domain.usergroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.domain.usergroup.controller.dto.UserGroupReqeustDTO;
import what.what2eat.domain.usergroup.entity.UserGroup;
import what.what2eat.domain.usergroup.repository.UserGroupRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupRepository userGroupRepository;
    private final AuthRepository authRepository;

    // 그룹 생성
    @Transactional
    public void createGroup(UserGroupReqeustDTO.CreateGroupDTO request) throws Exception {
        if (userGroupRepository.existsByUserGroupName(request.getGroupName())) {
            throw new Exception("이미 존재하는 그룹 이름입니다.");
        }

        User user = authRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException(" 존재하지 않는 사용자입니다. "));

        UserGroup userGroup = UserGroup.builder()
                .userGroupName(request.getGroupName())
                .userGroupCode(generateGroupCode())
                .build();

        userGroup.addUser(user);

        userGroupRepository.save(userGroup);
    }

    // 랜덤 그룹 코드 생성 메소드
    private String generateGroupCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
