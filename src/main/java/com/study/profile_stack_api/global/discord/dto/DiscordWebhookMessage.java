package com.study.profile_stack_api.global.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordWebhookMessage {

    /**
     * 봇의 사용자명 (옵션)
     */
    private String username;

    /**
     * 봇의 아바타 URL (옵션)
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    /**
     * 메시지 본문 (최대 2,000자)
     */
    private String content;
}
