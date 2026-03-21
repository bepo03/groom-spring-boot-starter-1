-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS profile_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 데이터베이스 사용
use profile_db;

-- 테이블 삭제
DROP TABLE IF EXISTS tech_stack;
DROP TABLE IF EXISTS profile;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS member;

-- 회원 테이블
CREATE TABLE IF NOT EXISTS member
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '회원 이름',
    password VARCHAR(100) NOT NULL COMMENT '비밀번호 (BCrypt)',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '권한 (USER, ADMIN, MANAGER)',
    enabled BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '회원 테이블';

-- Refresh Token 테이블
CREATE TABLE IF NOT EXISTS refresh_token
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Refresh Token ID',
    member_id BIGINT NOT NULL UNIQUE COMMENT '회원 ID (FK)',
    token VARCHAR(500) NOT NULL COMMENT '토큰',
    expiry_date TIMESTAMP NOT NULL COMMENT '만료 날짜',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT 'Refresh Token 테이블';

-- profile 테이블 생성
CREATE TABLE IF NOT EXISTS profile
(
    id BIGINT auto_increment PRIMARY KEY COMMENT '프로필 고유 ID',
    member_id BIGINT NOT NULL COMMENT '회원 ID (FK)',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    email VARCHAR(100) NOT NULL unique COMMENT '이메일',
    bio VARCHAR(500) COMMENT '자기소개',
    position VARCHAR(20) NOT NULL COMMENT '직무',
    career_years int NOT NULL default 0 COMMENT '경력 연차',
    github_url VARCHAR(200) COMMENT 'GitHub 주소',
    blog_url VARCHAR(200) COMMENT '블로그 주소',
    created_at timestamp default current_timestamp COMMENT '생성 일시',
    updated_at timestamp default current_timestamp ON UPDATE current_timestamp  COMMENT '수정 일시',
    FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '프로필 테이블';

-- tech_stack 테이블 생성
CREATE TABLE IF NOT EXISTS tech_stack
(
    id bigint auto_increment PRIMARY KEY COMMENT '기술 스택 고유 ID',
    profile_id bigint NOT NULL COMMENT '프로필 ID (FK)',
    name VARCHAR(50) NOT NULL COMMENT '기술명',
    category VARCHAR(20) NOT NULL COMMENT '기술 카테고리',
    proficiency VARCHAR(20) NOT NULL COMMENT '숙련도',
    years_of_exp int NOT NULL default 0 COMMENT '사용 경험 (년)',
    created_at timestamp default current_timestamp COMMENT '생성 일시',
    updated_at timestamp default current_timestamp ON UPDATE current_timestamp COMMENT '수정 일시',
    FOREIGN KEY(profile_id) references profile(id) ON DELETE cascade
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '기술 스택 테이블';

-- 인덱스 생성
CREATE INDEX idx_profile_position ON profile(position) ;
CREATE INDEX idx_tech_stack_profile_id ON tech_stack(profile_id) ;
CREATE INDEX idx_tech_stack_category ON tech_stack(category);
CREATE INDEX idx_profile_member_id ON profile(member_id);

-- 버전 컬럼 추가 (낙관적 락을 위한)
ALTER TABLE member ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE profile ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
