-- =====================
-- 1. User 더미 데이터
-- =====================
INSERT INTO users (type, email, password_hash, nickname, home_region_id, total_point, profile_id, kakao_id, is_profile_complete, created_at, updated_at)
VALUES
    ('USER', 'user1@example.com', 'hashed_pw1', 'Alice', 101, 100, NULL, NULL, true, NOW(), NOW()),
    ('USER', 'user2@example.com', 'hashed_pw2', 'Bob', 102, 50, NULL, NULL, false, NOW(), NOW());

