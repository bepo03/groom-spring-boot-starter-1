package com.study.profile_stack_api.domain.auth.service;

import com.study.profile_stack_api.domain.auth.dao.MemberDao;
import com.study.profile_stack_api.domain.auth.entity.Member;
import com.study.profile_stack_api.domain.auth.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 사용자별 세부 정보 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberDao memberDao;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        Member member = memberDao.findByUsername(username)
                .orElseThrow( () -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다. (username: " + username + ")"
                ));

        log.debug("User found: {}", member.getUsername());

        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .authorities(getAuthorities(member.getRole()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!member.isEnabled())
                .build();
    }

    /**
     * 회원 권한을 GrantedAuthority 컬렉션으로 변환
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (role != null) {
            String roleStr = "ROLE_" + role.name();
            authorities.add(new SimpleGrantedAuthority(roleStr));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}
