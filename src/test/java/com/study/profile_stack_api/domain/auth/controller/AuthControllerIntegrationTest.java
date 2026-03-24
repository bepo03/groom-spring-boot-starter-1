package com.study.profile_stack_api.domain.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.study.profile_stack_api.support.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends ControllerIntegrationTestSupport {

    @Test
    @DisplayName("회원가입부터 로그인, 토큰 재발급, 로그아웃까지 수행한다")
    void authFlow() throws Exception {
        String username = "authuser" + uniqueSuffix;
        String password = "password123";

        JsonNode signupData = signup(username, password);
        assertThat(signupData.path("username").asText()).isEqualTo(username);

        JsonNode loginData = login(username, password);
        String accessToken = loginData.path("accessToken").asText();
        String refreshToken = loginData.path("refreshToken").asText();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode refreshData = readBody(refreshResult).path("data");
        String refreshedAccessToken = refreshData.path("accessToken").asText();

        assertThat(refreshedAccessToken).isNotBlank();
        assertThat(refreshData.path("refreshToken").asText()).isNotBlank();
        assertThat(refreshData.path("tokenType").asText()).isEqualTo("Bearer");

        MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + refreshedAccessToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode logoutBody = readBody(logoutResult);
        assertThat(logoutBody.path("success").asBoolean()).isTrue();
        assertThat(logoutBody.path("data").asText()).contains("로그아웃");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401을 반환한다")
    void loginFailsWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", "javakim",
                                "password", "wrong-password"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급하면 401을 반환한다")
    void refreshFailsWithInvalidToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("refreshToken", "invalid-token"))))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode error = readBody(result).path("error");
        assertThat(error.path("code").asText()).isEqualTo("INVALID_TOKEN");
    }
}
