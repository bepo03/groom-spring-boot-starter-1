-- k6 realistic-load-test.js seed data
-- Creates profiles with IDs 1..10000 so random profile lookup returns 200.
-- Run this manually against a local/test database only.

USE profile_db;

SET SESSION cte_max_recursion_depth = 10000;

INSERT IGNORE INTO member (
    id,
    username,
    password,
    role,
    enabled
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10000
)
SELECT
    n,
    CONCAT('load-user-', n),
    '$2a$10$dummyPasswordForLoadTestOnly',
    'USER',
    TRUE
FROM seq;

INSERT IGNORE INTO profile (
    id,
    member_id,
    name,
    email,
    bio,
    position,
    career_years,
    github_url,
    blog_url
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10000
)
SELECT
    n,
    n,
    CONCAT('Load Test User ', n),
    CONCAT('load-user-', n, '@example.com'),
    'k6 realistic load test profile data',
    CASE MOD(n, 8)
        WHEN 0 THEN 'BACKEND'
        WHEN 1 THEN 'FRONTEND'
        WHEN 2 THEN 'FULLSTACK'
        WHEN 3 THEN 'MOBILE'
        WHEN 4 THEN 'DEVOPS'
        WHEN 5 THEN 'DATA'
        WHEN 6 THEN 'AI'
        ELSE 'ETC'
    END,
    MOD(n, 15),
    CONCAT('https://github.com/load-user-', n),
    CONCAT('https://blog.example.com/load-user-', n)
FROM seq;

SELECT COUNT(*) FROM profile WHERE id BETWEEN 1 AND 10000;
SELECT MIN(id), MAX(id) FROM profile;
