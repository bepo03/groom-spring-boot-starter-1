package com.study.profile_stack_api.global.discord.service;

import com.study.profile_stack_api.domain.auth.entity.Member;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import com.study.profile_stack_api.global.discord.dto.DiscordWebhookMessage;
import com.study.profile_stack_api.global.discord.dto.DiscordWebhookMessage.Embed;
import com.study.profile_stack_api.global.discord.dto.DiscordWebhookMessage.Field;
import com.study.profile_stack_api.global.discord.dto.DiscordWebhookMessage.Footer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Discord 알림 서비스
 *
 * Discord 채널에 알림을 전송
 * RestClient를 사용하여 Discord Webhook API와 통신
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    @Qualifier("discordRestClient")
    private final RestClient discordRestClient;

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    @Value("${discord.webhook.enabled}")
    private boolean webhookEnabled;

    @Value("${discord.webhook.username}")
    private String botUsername;

    @Value("${discord.webhook.avatar-url}")
    private String avatarUrl;

    // Discord Embed 색상 코드 (16진수)
    private static final int COLOR_SUCCESS = 0x00FF00;  // 녹색
    private static final int COLOR_INFO = 0x3498DB;     // 파란색
    private static final int COLOR_WARNING = 0xFFD700;  // 금색
    private static final int COLOR_ERROR = 0xFF0000;    // 빨간색

    /**
     * 새로운 Member 생성 알림
     *
     * @Async를 사용하여 비동기로 처리 (메인 로직을 블로킹하지 않음)
     *
     * @param member 생성된 Member 엔티티
     */
    @Async
    public void sendMemberCreatedNotification(Member member) {
        if (!webhookEnabled) {
            log.debug("Discord webhook is disabled. Skipping notification.");
            return;
        }

        try {
            DiscordWebhookMessage message = createMemberEmbed(
                    member,
                    "신규 회원가입 ✨",
                    COLOR_SUCCESS,
                    "New Member"
            );

            sendWebhookMessage(message);
            log.info("Successfully sent Discord notification for Member ID: {}", member.getId());
        } catch (Exception e) {
            // Discord 알림 실패가 메인 비즈니스 로직에 영향을 주면 안됨
            log.error("Failed to send Discord notification for Member ID: {}", member.getId());

        }
    }

    /**
     * 새로운 Profile 생성 알림
     *
     * @Async를 사용하여 비동기로 처리 (메인 로직을 블로킹하지 않음)
     *
     * @param profile 생성된 Profile 엔티티
     */
    @Async
    public void sendProfileCreatedNotification(Profile profile) {
        if (!webhookEnabled) {
            log.debug("Discord webhook is disabled. Skipping notification.");
            return;
        }

        try {
            DiscordWebhookMessage message = createProfileEmbed(
                    profile,
                    "새로운 프로필이 생성되었습니다!",
                    COLOR_SUCCESS,
                    "✨ Created Profile"
            );

            sendWebhookMessage(message);
            log.info("Successfully sent Discord notification for Profile ID: {}", profile.getId());
        } catch (Exception e) {
            // Discord 알림 실패가 메인 비즈니스 로직에 영향을 주면 안됨
            log.error("Failed to send Discord notification for Profile ID: {}", profile.getId(), e);

        }
    }

    /**
     * Profile 수정 알림
     *
     * @param profile 수정된 Profile 엔티티
     */
    @Async
    public void sendProfileUpdatedNotification(Profile profile) {
        if (!webhookEnabled) {
            log.debug("Discord webhook is disabled. Skipping notification.");
            return;
        }

        try {
            DiscordWebhookMessage message = createProfileEmbed(
                    profile,
                    "프로필이 수정되었습니다.",
                    COLOR_INFO,
                    "📝 Update Profile"
            );

            sendWebhookMessage(message);
        } catch (Exception e) {
            log.error("Failed to send Discord update notification for Profile ID: {}", profile.getId(), e);
        }
    }

    /**
     * Profile 삭제 알림
     *
     * @param profileId 삭제된 Profile ID
     * @param username 삭제된 Profile 사용자 이름
     */
    @Async
    public void sendProfileDeletedNotification(Long profileId, String username) {
        if (!webhookEnabled) {
            log.debug("Discord webhook is disabled. Skipping notification.");
            return;
        }

        try {
            DiscordWebhookMessage message = DiscordWebhookMessage.builder()
                    .username(botUsername)
                    .avatarUrl(avatarUrl)
                    .embeds(List.of(
                            Embed.builder()
                                    .title("🗑️ Profile Deleted")
                                    .description("프로필이 삭제되었습니다.")
                                    .color(COLOR_WARNING)
                                    .fields(List.of(
                                            Field.builder()
                                                    .name("ID")
                                                    .value(String.valueOf(profileId))
                                                    .inline(true)
                                                    .build(),
                                            Field.builder()
                                                    .name("이름")
                                                    .value(username)
                                                    .inline(true)
                                                    .build()
                                    ))
                                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                    .build()
                    ))
                    .build();

            sendWebhookMessage(message);
        } catch (Exception e) {
            log.error("Failed to send Discord delete notification for Profile ID: {}", profileId, e);
        }
    }

    /**
     * Member를 Discord Embed 메시지로 변환
     *
     * @param member Member 엔티티
     * @param title Embed 제목
     * @param color Embed 색상
     * @param footerText 푸터 텍스트
     * @return Discord 메시지 객체
     */
    private DiscordWebhookMessage createMemberEmbed(Member member, String title, int color, String footerText) {
        List<Field> fields = new ArrayList<>();

        fields.add(Field.builder()
                .name("👤 이름")
                .value(member.getUsername())
                .inline(false)
                .build());

        fields.add(Field.builder()
                .name("🛡️ 권한")
                .value(member.getRole().getIcon() + " " +
                        member.getRole().name())
                .inline(true)
                .build());

        // Embed 생성
        Embed embed = Embed.builder()
                .title(title)
                .color(color)
                .fields(fields)
                .footer(Footer.builder()
                        .text(footerText + " ID: " + member.getId() + " | ")
                        .build())
                .timestamp(Instant.now().toString())
                .build();

        // 메시지 생성
        return DiscordWebhookMessage.builder()
                .username(botUsername)
                .avatarUrl(avatarUrl)
                .embeds(List.of(embed))
                .build();
    }

    /**
     * Profile를 Discord Embed 메시지로 변환
     *
     * @param profile Profile 엔티티
     * @param title Embed 제목
     * @param color Embed 색상
     * @param footerText 푸터 텍스트
     * @return Discord 메시지 객체
     */
    private DiscordWebhookMessage createProfileEmbed(Profile profile, String title, int color, String footerText) {
        List<Field> fields = new ArrayList<>();

        fields.add(Field.builder()
                .name("👤 이름")
                .value(profile.getName())
                .inline(true)
                .build());

        fields.add(Field.builder()
                .name("📧 이메일")
                .value(profile.getEmail())
                .inline(true)
                .build());

        String bio = profile.getBio();
        if (bio != null && bio.length() > 100) {
            bio = bio.substring(0, 97) + "...";
        }

        fields.add(Field.builder()
                .name("📝 소개")
                .value(bio != null ? bio : "자기소개 없음")
                .inline(false)
                .build());

        fields.add(Field.builder()
                .name("💼 포지션")
                .value(profile.getPosition().getIcon() + " " +
                        profile.getPosition().getDescription())
                .inline(true)
                .build());

        fields.add(Field.builder()
                .name("📈 경력")
                .value(profile.getCareerYears() + "년")
                .inline(true)
                .build());

        String githubUrl = profile.getGithubUrl();
        fields.add(Field.builder()
                .name("🐙 GitHub")
                .value(githubUrl != null ? githubUrl : "Github 주소 없음")
                .inline(false)
                .build());

        String blogUrl = profile.getBlogUrl();
        fields.add(Field.builder()
                .name("🔗 블로그")
                .value(blogUrl != null ? blogUrl : "블로그 주소 없음")
                .inline(false)
                .build());

        // Embed 생성
        Embed embed = Embed.builder()
                .title(title)
                .color(color)
                .fields(fields)
                .footer(Footer.builder()
                        .text(footerText + " ID: " + profile.getId() + " | ")
                        .build())
                .timestamp(Instant.now().toString())
                .build();

        // 메시지 생성
        return DiscordWebhookMessage.builder()
                .username(botUsername)
                .avatarUrl(avatarUrl)
                .embeds(List.of(embed))
                .build();
    }

    /**
     * Discord Webhook으로 메시지 전송
     *
     * @param message 전송할 메시지
     */
    private void sendWebhookMessage(DiscordWebhookMessage message) {
        try {
            discordRestClient.post()
                    .uri(webhookUrl)
                    .body(message)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) ->  {
                        log.error("Discord API Client Error: {} - {}",
                                response.getStatusCode(), response.getStatusText());
                        throw new RestClientException("Discord API 요청 실패: " + response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Discord API Server Error: {} - {}",
                                response.getStatusCode(), response.getStatusText());
                        throw new RestClientException("Discord 서버 오류: " + response.getStatusCode());
                    })
                    .toBodilessEntity(); // Discord Webhook은 응답 본문이 없음

            log.debug("Discord webhook message sent successfully");
        } catch (RestClientException e) {
            log.error("Failed to send Discord webhook message", e);
            throw e;
        }
    }

    /**
     * 테스트용 알림 전송
     *
     * @return 전송 성공 여부
     */
    public boolean sendTestNotification() {
        if (!webhookEnabled) {
            log.info("Discord webhook is disabled");
            return false;
        }

        try {
            DiscordWebhookMessage message = DiscordWebhookMessage.builder()
                    .username(botUsername)
                    .avatarUrl(avatarUrl)
                    .content("\uD83C\uDF89 Discord 연동 테스트가 성공했습니다! Profile & Tech Stack Bot이 정상적으로 작동합니다.")
                    .build();

            sendWebhookMessage(message);
            return true;
        } catch (Exception e) {
            log.error("Test notification failed", e);
            return false;
        }
    }
}
