package com.study.profile_stack_api.domain.auth.service;

import com.study.profile_stack_api.domain.auth.dao.MemberDao;
import com.study.profile_stack_api.domain.auth.dao.RefreshTokenDao;
import com.study.profile_stack_api.domain.auth.dto.request.LoginRequest;
import com.study.profile_stack_api.domain.auth.dto.request.SignupRequest;
import com.study.profile_stack_api.domain.auth.dto.request.TokenRefreshRequest;
import com.study.profile_stack_api.domain.auth.dto.response.LoginResponse;
import com.study.profile_stack_api.domain.auth.dto.response.SignupResponse;
import com.study.profile_stack_api.domain.auth.dto.response.TokenRefreshResponse;
import com.study.profile_stack_api.domain.auth.entity.Member;
import com.study.profile_stack_api.domain.auth.entity.RefreshToken;
import com.study.profile_stack_api.domain.auth.entity.Role;
import com.study.profile_stack_api.domain.auth.exception.DuplicateResourceException;
import com.study.profile_stack_api.domain.auth.exception.InvalidTokenException;
import com.study.profile_stack_api.global.exception.AuthException;
import com.study.profile_stack_api.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 인증 서비스
 * 로그인, 회원가입 및 토큰 갱신 작업을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenDao refreshTokenDao;
    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * 회원 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());

            // 1. AuthenticationManager로 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()
                    )
            );

            // 사용자 및 권한 목록 가져오기
            Member member = memberDao.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다."));

            String roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // 2. Access Token + Refresh Token 생성
            String accessToken = jwtTokenProvider.createAccessToken(member.getUsername(), roles);
            RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getUsername());

            // 3. Refresh Token DB 저장
            refreshTokenDao.saveRefreshToken(refreshToken);

            log.info("Login successful for member: {}", member.getUsername());

            return LoginResponse.of(
                    accessToken,
                    refreshToken.getToken(),
                    accessTokenExpiration,
                    member.getUsername()
            );
        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            throw new BadCredentialsException("사용자 이름 또는 비밀번호가 잘못되었습니다.");
        }
    }

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("Signup attempt for username: {}", request.getUsername());

        if (memberDao.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("이미 사용 중인 아이디입니다. (username: " + request.getUsername() + ")");
        }

        Member newMember = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        Member savedMember = memberDao.save(newMember);

        log.info("Member registered successfully: {}", savedMember.getUsername());

        return SignupResponse.of(
                savedMember.getId(),
                savedMember.getUsername()
        );
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        log.info("Token refresh attempt");

        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("잘못된 토큰입니다.");
        }

        if(!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("리프레시 토큰이 아닙니다.");
        }

        // 2. DB에 저장된 Refresh Token과 일치하는지 확인
        Member member = memberDao.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("리프레시 토큰을 찾을 수 없거나 만료되었습니다."));

        Role role = member.getRole() != null ? member.getRole() : Role.USER;
        String roles = "ROLE_" + role.name();

        // 3. 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(member.getUsername(), roles);
        RefreshToken newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getUsername());

        refreshTokenDao.saveRefreshToken(newRefreshToken);

        log.info("Token refreshed successfully for member: {}", member.getUsername());

        return TokenRefreshResponse.of(
                newAccessToken,
                newRefreshToken.getToken(),
                accessTokenExpiration
        );
    }

    /**
     * 로그아웃 (액세스 토큰 인증 사용자 기준)
     */
    @Transactional
    public void logout(String username) {
        log.info("Logout attempt for username: {}", username);

        Member member = memberDao.findByUsername(username)
                .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다."));

        refreshTokenDao.deleteByMemberId(member.getId());
        log.info("Logout successful for username: {}", username);
    }
}
