-- 회원 데이터 (비밀번호는 BCrypt로 인코딩된 "password123")
# INSERT INTO member (username, password, role) VALUES
#     ('javakim', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
#     ('reactlee', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER'),
#     ('fullpark', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER');

-- 프로필 데이터 (member_id 추가)
INSERT INTO profile (name, email, bio, position, career_years, github_url, blog_url) VALUES
    ('김자바', 'java.kim@example.com', 'Spring Boot를 사랑하는 백엔드 개발자입니다.', 'BACKEND', 3, 'https://github.com/javakim', 'https://javakim.tistory.com'),
    ('이리액트', 'react.lee@example.com', '사용자 경험을 중시하는 프론트엔드 개발자입니다.', 'FRONTEND', 2, 'https://github.com/reactlee', NULL),
    ('박풀스택', 'full.park@example.com', '프론트부터 백엔드까지 다루는 풀스택 개발자입니다.', 'FULLSTACK', 5, 'https://github.com/fullpark', 'https://fullpark.dev');

-- 기술 스택 데이터 (김자바)
INSERT INTO tech_stack
(profile_id, name, category, proficiency, years_of_exp)
VALUES
    (1, 'Java', 'LANGUAGE', 'ADVANCED', 3),
    (1, 'Spring Boot', 'FRAMEWORK', 'INTERMEDIATE', 2),
    (1, 'MySQL', 'DATABASE', 'INTERMEDIATE', 2),
    (1, 'Git', 'TOOL', 'ADVANCED', 3),
    (1, 'Docker', 'DEVOPS', 'BEGINNER', 1);

-- 기술 스택 데이터 (이리액트)
INSERT INTO tech_stack
(profile_id, name, category, proficiency, years_of_exp)
VALUES
    (2, 'JavaScript', 'LANGUAGE', 'ADVANCED', 2),
    (2, 'TypeScript', 'LANGUAGE', 'INTERMEDIATE', 1),
    (2, 'React', 'FRAMEWORK', 'ADVANCED', 2),
    (2, 'Git', 'TOOL', 'INTERMEDIATE', 2);

-- 기술 스택 데이터 (박풀스택)
INSERT INTO tech_stack
(profile_id, name, category, proficiency, years_of_exp)
VALUES
    (3, 'Java', 'LANGUAGE', 'EXPERT', 5),
    (3, 'JavaScript', 'LANGUAGE', 'ADVANCED', 4),
    (3, 'Spring Boot', 'FRAMEWORK', 'ADVANCED', 4),
    (3, 'React', 'FRAMEWORK', 'INTERMEDIATE', 2),
    (3, 'PostgreSQL', 'DATABASE', 'ADVANCED', 3),
    (3, 'Redis', 'DATABASE', 'INTERMEDIATE', 2),
    (3, 'Docker', 'DEVOPS', 'ADVANCED', 3),
    (3, 'Kubernetes', 'DEVOPS', 'BEGINNER', 1);
