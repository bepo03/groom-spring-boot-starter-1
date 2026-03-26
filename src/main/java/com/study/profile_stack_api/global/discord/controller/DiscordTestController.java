package com.study.profile_stack_api.global.discord.controller;

import com.study.profile_stack_api.global.discord.service.DiscordNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Discord", description = "Discord 알림 테스트 API")
@RestController
@RequestMapping("/api/v1/discord")
@RequiredArgsConstructor
public class DiscordTestController {

    private final DiscordNotificationService discordNotificationService;

    @Operation(
            summary = "Discord 알림 테스트",
            description = "Discord Webhook 연동이 정상 작동하는지 테스트합니다."
    )
    @PostMapping("/test")
    public ResponseEntity<String> testDiscordNotification() {
        boolean success = discordNotificationService.sendTestNotification();

        if (success) {
            return ResponseEntity.ok("Discord 알림 테스트 성공! 채널을 확인하세요.");
        } else {
            return ResponseEntity.internalServerError()
                    .body("Discord 알림 테스트 실패. 설정을 확인하세요.");
        }
    }
}
