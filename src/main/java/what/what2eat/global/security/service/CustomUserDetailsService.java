package what.what2eat.global.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import what.what2eat.domain.auth.entity.User;
import what.what2eat.domain.auth.repository.AuthRepository;
import what.what2eat.global.security.domain.CustomUserDetails;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {

        //DB에서 유저 정보 조회
        User user = authRepository.findByUserEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.info("유저 정보 조회 완료");

        return CustomUserDetails.builder()
                .email(user.getUserEmail())
                .password(user.getPassword())
                .provider(user.getProvider())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())))
                .build();
    }
}
