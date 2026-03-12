package com.study.profile_stack_api.global.security.jwt;

import com.study.profile_stack_api.domain.auth.exception.ExpiredTokenException;
import com.study.profile_stack_api.domain.auth.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청마다 JWT를 검사해 SecurityContext에 인증 정보를 세팅하는 필터.
 *
 * 동작 순서:
 * 1) Authorization 헤더에서 Bearer 토큰 추출
 * 2) 토큰 검증 및 사용자 조회
 * 3) Authentication 생성 후 SecurityContext 저장
 * 4) 예외는 request attribute("exception")에 저장해 EntryPoint로 위임
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull
            HttpServletRequest request,
            @NonNull
            HttpServletResponse response,
            @NonNull
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. Authorization 헤더에서 토큰 추출
            String token = resolveToken(request);

            // 2. 토큰이 유효하면 SecurityContext에 Authentication 설정
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredTokenException | InvalidTokenException e) {
            // JWT 관련 예외는 request에 저장하여 EntryPoint에서 처리
            log.error("JWT authentication failed: {}", e.getMessage());

            request.setAttribute("exception", e);
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);

            request.setAttribute("exception", e);
        }

        // 3. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 인증이 필요하지 않은 경로는 필터를 건너뛴다.
     * 로그인/회원가입/토큰 재발급은 토큰 검증이 불필요하다.
     * 로그아웃은 access token 인증이 필요하므로 필터를 통과시킨다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/signup")
                || path.equals("/api/v1/auth/refresh");
    }

    /**
     * Authorization 헤더에서 "Bearer " 접두사를 제거하고 토큰 값만 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
