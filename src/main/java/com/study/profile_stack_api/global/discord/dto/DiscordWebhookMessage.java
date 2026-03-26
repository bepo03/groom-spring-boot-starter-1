package com.study.profile_stack_api.global.discord.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Discord Webhook 메시지 DTO
 * Discord Webhook API 스펙에 맞춰 설계
 *
 * @see <a href="https://discord.com/developers/docs/resources/webhook">Discord Webhook Documentation</a>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordWebhookMessage {

    /**
     * 봇의 사용자명 (옵션)
     * 웹훅 설정의 기본 값을 덮어씀
     */
    private String username;

    /**
     * 봇의 아바타 URL (옵션)
     * 웹훅 설정의 기본값을 덮어씀
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    /**
     * 메시지 본문 (최대 2,000자)
     * Embed를 사용하지 않을 때 사용
     */
    private String content;

    /**
     * TTS(Text-to-Speech) 사용 여부
     */
    private  boolean tts;

    /**
     * Embed 메시지 리스트 (최대 10개)
     * 리치 콘텐츠를 표시할 때 사용
     */
    private List<Embed> embeds;

    /**
     * Discord Embed 객체
     * 리치 포맷의 메시지를 만들 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Embed {

        /**
         * Embed 제목
         */
        private String title;

        /**
         * Embed 타입 (보통 "rich" 사용)
         */
        private String type;

        /**
         * Embed 설명 (본문)
         */
        private String description;

        /**
         * 제목 클릭 시 이동할 URL
         */
        private String url;

        /**
         * 타임스탬프 (ISO8601형식)
         */
        private String timestamp;

        /**
         * Embed 색상 (16진수 정수)
         * 예: 0x00FF00 (녹색)
         */
        private Integer color;

        /**
         * 푸터 정보
         */
        private Footer footer;

        /**
         * 썸네일 이미지
         */
        private Image thumbnail;

        /**
         * 메인 이미지
         */
        private Image image;

        /**
         * 작성자 정보
         */
        private Author author;

        /**
         * 필드 리스트 (최대 25개)
         */
        private List<Field> fields;
    }

    /**
     * Embed 푸터
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Footer {
        private String text;

        @JsonProperty("icon_url")
        private String iconUrl;
    }

    /**
     * Embed 이미지
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Image {
        private String url;
        private Integer height;
        private Integer width;
    }

    /**
     * Embed 작성자
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Author {
        private String name;
        private String url;

        @JsonProperty("icon_url")
        private String iconUrl;
    }

    /**
     * Embed 필드
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Field {

        /**
         * 필드 이름
         */
        private String name;

        /**
         * 필드 값
         */
        private String value;

        /**
         * 인라인 표시 여부
         * true: 같은 줄에 표시, false: 새 줄에 표시
         */
        private boolean inline;
    }
}
