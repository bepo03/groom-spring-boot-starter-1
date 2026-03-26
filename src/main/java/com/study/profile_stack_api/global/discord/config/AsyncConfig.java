package com.study.profile_stack_api.global.discord.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 *
 * Discord 알림 등 외부 API 호출을 비동기로 처리하기 위한 설정
 * @EnableAsync를 통해 @Async 어노테이션 활성화
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 작업용 ThreadPool 설정
     *
     * @return 커스텀 설정된 TaskExecutor
     */
    @Bean(name = "asyncExecutor")
    public Executor ayncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 스레드 개수
        executor.setCorePoolSize(2);

        // 최대 스레드 개수
        executor.setMaxPoolSize(10);

        // 큐 용량 (대기 작업 최대 개수)
        executor.setQueueCapacity(100);

        // 스레드 이름 prefix
        executor.setThreadNamePrefix("Discord-");

        // 스레드 유휴 시간 (초)
        executor.setKeepAliveSeconds(60);

        // 애플리케이션 종료 시 처리되지 않은 작업 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 종료 대기 시간 (초)
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        return executor;
    }
}
