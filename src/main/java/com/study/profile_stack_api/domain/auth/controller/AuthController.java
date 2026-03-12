package com.study.profile_stack_api.domain.auth.controller;

import com.study.profile_stack_api.domain.auth.dto.request.LoginRequest;
import com.study.profile_stack_api.domain.auth.dto.request.SignupRequest;
import com.study.profile_stack_api.domain.auth.dto.request.TokenRefreshRequest;
import com.study.profile_stack_api.domain.auth.dto.response.LoginResponse;
import com.study.profile_stack_api.domain.auth.dto.response.SignupResponse;
import com.study.profile_stack_api.domain.auth.dto.response.TokenRefreshResponse;
import com.study.profile_stack_api.domain.auth.service.AuthService;
import com.study.profile_stack_api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 REST 컨트롤러
 * 사용자 인증 및 등록을 위한 엔트포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자 로그인
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        log.info("Login request for username: {}", request.getUsername());

        // 로그인 서비스 호출
        LoginResponse response = authService.login(request);

        // 200 OK 상태코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(response));
    }

    /**
     * 사용자 회원가입
     * POST /api/v1/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid
            @RequestBody
            SignupRequest request
    ) {
        log.info("Signup reqeust for username: {}", request.getUsername());

        // 회원가입 서비스 호출
        SignupResponse response = authService.signup(request);

        // 201 CREATED 상태코드와 함께 응답
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 액세스 토큰 재발급
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> signup(
            @Valid
            @RequestBody
            TokenRefreshRequest request
    ) {
        log.info("Token refresh request");

        // 재발급 서비스 호출
        TokenRefreshResponse response = authService.refresh(request);

        // 200 OK 상태코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(response));
    }

    /**
     * 사용자 로그아웃
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("Logout request for username: {}", username);

        // 로그아웃 서비스 호출
        authService.logout(username);

        // 200 OK 상태코드와 함께 응답
        return ResponseEntity
                .ok()
                .body(ApiResponse.success("로그아웃이 성공적으로 완료되었습니다."));
    }
}
