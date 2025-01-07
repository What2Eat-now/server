package what.what2eat.domain.usergroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.domain.usergroup.controller.dto.UserGroupReqeustDTO;
import what.what2eat.domain.usergroup.entity.UserGroup;
import what.what2eat.domain.usergroup.repository.UserGroupRepository;

import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupRepository userGroupRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    // 그룹 생성
    @Transactional
    public void createUserGroup(UserGroupReqeustDTO.CreateUserGroupDTO request) {
        if (userGroupRepository.existsByUserGroupName(request.getGroupName())) {
            throw new IllegalStateException("이미 존재하는 그룹 이름입니다.");
        }

        // user 객체 조회
        User user = authRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException(" 존재하지 않는 사용자입니다. "));

        // UserGroup 객체 build
        UserGroup userGroup = UserGroup.builder()
                .userGroupName(request.getGroupName())
                .owner(request.getUserEmail())
                .userGroupPassword(passwordEncoder.encode(request.getGroupPassword()))
                .build();

        // 양방향 연관관계 설정
        userGroup.addUser(user);

        userGroupRepository.save(userGroup);
    }


    // 그룹 가입
    @Transactional
    public void joinUserGroup(Long groupId, UserGroupReqeustDTO.JoinUserGroupDTO request) {
        UserGroup userGroup = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        User user = authRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));

        // 가입 여부 검사
        boolean isRegistered = userGroup.getUsers().stream()
                .anyMatch(u -> u.getUserEmail().equals(user.getUserEmail()));

        if (isRegistered) {
            throw new IllegalStateException("이미 그룹에 가입되어 있습니다.");
        }

        // 비밀번호 유효성 검사
        if (!passwordEncoder.matches(request.getGroupPassword(), userGroup.getUserGroupPassword())) {
            throw new IllegalArgumentException("유효하지 않는 비밀번호 입니다.");
        }

        // 가입 처리
        userGroup.addUser(user);
    }

    @Transactional
    public void withDrawUserGroup(Long groupId, Long userId) {
        UserGroup userGroup = userGroupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 그룹입니다."));

        User user = authRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));

        // 가입 여부 검사
        boolean isRegistered = userGroup.getUsers().stream()
                .anyMatch(u -> u.getUserEmail().equals(user.getUserEmail()));

        if (!isRegistered) {
            throw new IllegalStateException("그룹에 가입되어 있지 않습니다.");
        }

        userGroup.removeUser(user);
    }

}
