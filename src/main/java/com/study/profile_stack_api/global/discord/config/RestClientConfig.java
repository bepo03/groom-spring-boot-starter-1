package com.study.profile_stack_api.global.discord.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * Discord Webhook 전용 RestClient
     */
    @Bean(name = "discordRestClient")
    public RestClient discordRestClient() {

        // 타입 아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 연결: 5초
        factory.setConnectTimeout(Duration.ofSeconds(5));

        // 읽기: 10초
        factory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Profile & Tech Stack Bot/1.0")
                .requestFactory(factory)
                .build();
    }
}
