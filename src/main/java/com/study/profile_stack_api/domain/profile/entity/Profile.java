package com.study.profile_stack_api.domain.profile.entity;

import com.study.profile_stack_api.domain.auth.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 프로필 Entity
 */
@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Position position;

    @Column(name = "career_years", nullable = false)
    private Integer careerYears;

    @Column(name = "github_url", length = 200)
    private String githubUrl;

    @Column(name = "blog_url", length = 200)
    private String blogUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;
}
