package com.study.profile_stack_api;

import com.study.profile_stack_api.domain.profile.dao.ProfileDao;
import com.study.profile_stack_api.domain.profile.entity.Position;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import com.study.profile_stack_api.domain.techstack.dao.TechStackDao;
import com.study.profile_stack_api.domain.techstack.entity.Proficiency;
import com.study.profile_stack_api.domain.techstack.entity.TechCategory;
import com.study.profile_stack_api.domain.techstack.entity.TechStack;
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
class TechStackDaoPagingTest {
    @Autowired
    private TechStackDao techStackDao;

    @Autowired
    private ProfileDao profileDao;

    private Long profileId;

    @BeforeEach
    void setup() {
        // 기존 데이터 초기화 후 테스트 데이터 생성
        profileDao.deleteAll();

        // 테스트용 프로필 생성 (FK 대상)
        Profile profile = new Profile();
        profile.setMemberId(1L);
        profile.setName("테스트 유저");
        profile.setEmail("test@test.com");
        profile.setBio("테스트");
        profile.setPosition(Position.BACKEND);
        profile.setCareerYears(1);
        profile.setGithubUrl("https://github.com/test");
        profile.setBlogUrl("https://test.tistory.com");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        Profile savedProfile = profileDao.save(profile);
        profileId = savedProfile.getId();

        // 테스트용 기술 스택 25건 생성
        for (int i = 1; i <= 25; i++) {
            TechCategory category = i % 2 == 0 ? TechCategory.LANGUAGE : TechCategory.FRAMEWORK;
            Proficiency proficiency = i % 3 == 0 ? Proficiency.ADVANCED : Proficiency.INTERMEDIATE;

            TechStack techStack = TechStack.builder()
                    .id(null)
                    .profileId(profileId)
                    .name("기술 " + i)
                    .category(category)
                    .proficiency(proficiency)
                    .yearsOfExp(i % 10)
                    .build();

            techStackDao.saveByProfileId(profileId, techStack);
        }
    }

    @Test
    @DisplayName("첫 번째 페이지 조회 - 10건")
    void findAllWithPaging_firstPage() {
        // when
        Page<TechStack> result = techStackDao.findAllWithPaging(profileId, 0, 10);

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
        Page<TechStack> result = techStackDao.findAllWithPaging(profileId, 2, 10);

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
        Page<TechStack> result = techStackDao.findAllWithPaging(profileId, 10, 10);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("카테고리별 페이징 조회")
    void findByCategoryWithPaging() {
        // when
        Page<TechStack> result = techStackDao.findByCategoryWithPaging(profileId, "LANGUAGE", 0, 5);

        // then
        assertThat(result.getContent())
                .allMatch(stack -> stack.getCategory() == TechCategory.LANGUAGE);
        assertThat(result.getTotalElements()).isLessThanOrEqualTo(25);
    }

    @Test
    @DisplayName("검색 + 페이징 조회")
    void searchWithPaging() {
        // when
        Page<TechStack> result = techStackDao.searchWithPaging(
                profileId,
                "FRAMEWORK",
                "ADVANCED",
                0,
                10
        );

        // then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent())
                .allMatch(stack -> stack.getCategory() == TechCategory.FRAMEWORK
                        && stack.getProficiency() == Proficiency.ADVANCED);
    }
}
