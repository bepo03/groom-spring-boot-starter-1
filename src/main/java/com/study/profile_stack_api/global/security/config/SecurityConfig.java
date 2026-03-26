package com.study.profile_stack_api.global.security.config;

import com.study.profile_stack_api.global.security.jwt.JwtAuthenticationEntryPoint;
import com.study.profile_stack_api.global.security.jwt.JwtAuthenticationFilter;
import com.study.profile_stack_api.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 전역 설정 클래스
 * - 인증/인가 규칙 설정
 * - 로그인 및 필터 체인 구성
 * - 비밀번호 인코더 등 보안 관련 Bean 등록
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Spring Security 필터 체인 구성
     *
     * @param http HttpSecurity 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 중 예외가 발생한 경우
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. REST API 이므로 CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Stateless 세션 (JWT이므로 세션 사용하지 않음)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 인가 규칙
                .authorizeHttpRequests(auth ->
                        auth
                                // 로그아웃은 인증된 사용자만 가능
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()

                                // 인증 관련 API는 로그인 전에도 접근 허용
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()

                                // 프로필 및 기술 스택 조회 API는 누구나 접근 가능
                                .requestMatchers(HttpMethod.GET, "/api/v1/profiles/**").permitAll()

                                // Swagger UI 및 API 문서 접근 허용
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                                // Discord API Test 허용
                                .requestMatchers(HttpMethod.POST, "/api/v1/discord/test").permitAll()

                                // 프로필 생성은 로그인한 사용자만 가능
                                .requestMatchers(HttpMethod.POST, "/api/v1/profiles").authenticated()

                                // 프로필 수정/삭제는 로그인한 사용자만 가능
                                .requestMatchers(HttpMethod.PUT, "/api/v1/profiles/*").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/profiles").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/profiles/*").authenticated()

                                // 기술 스택 생성은 로그인한 사용자만 가능
                                .requestMatchers(HttpMethod.POST, "/api/v1/profiles/*/tech-stacks").authenticated()

                                // 기술 스택 수정/삭제는 로그인한 사용자만 가능
                                .requestMatchers(HttpMethod.PUT, "/api/v1/profiles/*/tech-stacks/*").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/profiles/*/tech-stacks").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/profiles/*/tech-stacks/*").authenticated()

                                // 정의되지 않은 나머지 요청은 모두 차단
                                .anyRequest().denyAll()
                )

                // 4. 미인증 / 토큰 에러 시 JSON 응답
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authenticationEntryPoint)
                )

                // 5. JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * AuthenticationManager 빈 등록
     *
     * @param configuration 인증 설정 객체
     * @return AuthenticationManager
     * @throws Exception 인증 매니저 생성 중 예외가 발생한 경우
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈 등록
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
