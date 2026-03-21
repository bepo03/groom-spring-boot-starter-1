package com.study.profile_stack_api;

import com.study.profile_stack_api.domain.auth.entity.Member;
import com.study.profile_stack_api.domain.auth.entity.Role;
import com.study.profile_stack_api.domain.profile.dao.ProfileDao;
import com.study.profile_stack_api.domain.profile.entity.Position;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import com.study.profile_stack_api.global.common.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(classes = ProfileStackApiApplication.class)
@Transactional
class ProfileDaoPagingTest {
    @Autowired
    private ProfileDao profileDao;

    @BeforeEach
    void setup() {
        // 기존 데이터 초기화 후 테스트 데이터 생성
        profileDao.deleteAll();

        Member member = Member.builder()
                .id(1L)
                .username("테스트")
                .password("테스트")
                .role(Role.USER)
                .build();

        // 테스트용 프로필 25건 생성
        for (int i = 1; i <= 25; i++) {
            Position position = i % 2 == 0 ? Position.BACKEND
                    : i % 3 == 1 ? Position.FRONTEND
                    : Position.FULLSTACK;

            Profile profile = new Profile();
            profile.setName("이름 " + i);
            profile.setMemberId(1L);
            profile.setEmail("test" + i + "@test.com");
            profile.setBio("내용 " + i);
            profile.setPosition(position);
            profile.setCareerYears(i % 10);
            profile.setGithubUrl("https://github.com/" + i);
            profile.setBlogUrl("https://" + i + ".tistory.com");
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());

            profileDao.save(profile);
        }
    }

    @Test
    @DisplayName("첫 번째 페이지 조회 - 10건")
    void findAllWithPaging_firstPage() {
        // when
        Page<Profile> result = profileDao.findAllWithPaging(0, 10);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.isHasPrevious()).isFalse();
    }

    @Test
    @DisplayName("마지막 페이지 조회 - 5건")
    void findAllWithPaging_lastPage() {
        // when
        Page<Profile> result = profileDao.findAllWithPaging(2, 10);

        // then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 페이지 조회 - 빈 결과")
    void findAllWithPaging_emptyPage() {
        // when
        Page<Profile> result = profileDao.findAllWithPaging(10, 10);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("직무별 페이징 조회")
    void findByPositionWithPaging() {
        // when
        Page<Profile> result = profileDao.findByPositionWithPaging("BACKEND", 0, 5);

        // then
        assertThat(result.getContent())
                .allMatch(log -> log.getPosition() == Position.BACKEND);
        assertThat(result.getTotalElements()).isLessThanOrEqualTo(25);
    }

    @Test
    @DisplayName("검색 + 페이징 조회")
    void searchWithPaging() {
        // when
        Page<Profile> result = profileDao.searchWithPaging(
                "이름",
                "FRONTEND",
                0, 10);

        // then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent())
                .allMatch(log -> log.getName().contains("이름"));
        assertThat(result.getContent())
                .allMatch(stack -> stack.getPosition() == Position.FRONTEND);
    }
}
