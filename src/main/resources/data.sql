-- =====================
-- 기존 데이터 초기화
-- =====================
DELETE FROM survey_option;
DELETE FROM survey_item;
DELETE FROM users;


-- =====================
-- 1. User 더미 데이터
-- =====================
INSERT INTO users (type, email, password_hash, nickname, home_region_id, total_point, profile_id, kakao_id, is_profile_complete, created_at, updated_at)
VALUES
    ('USER', 'user1@example.com', 'hashed_pw1', 'Alice', 101, 100, NULL, NULL, true, NOW(), NOW()),
    ('USER', 'user2@example.com', 'hashed_pw2', 'Bob', 102, 50, NULL, NULL, false, NOW(), NOW());

-- =====================
-- 2. SurveyItem 더미 데이터
-- =====================
INSERT INTO survey_item (id, code, question, created_at, updated_at) VALUES
                                                                         (1, '분리배출', '오늘 재활용품(종이, 플라스틱, 캔 등)을 올바르게 분리배출하셨나요?', NOW(), NOW()),
                                                                         (2, '일회용품', '오늘 일회용품을 얼마나 사용하셨나요?', NOW(), NOW()),
                                                                         (3, '종이 타월사용', '오늘 종이 타월을 얼마나 사용하셨나요?', NOW(), NOW()),
                                                                         (4, '음식물쓰레기', '오늘 음식물쓰레기를 남기셨나요?', NOW(), NOW()),
                                                                         (5, '에너지절약', '오늘 사용자히 않는 전자기기 전원을 껐나요?', NOW(), NOW()),
                                                                         (6, '새로운 시도', '오늘 환경을 위해 새로운 시도를 해보셨나요?', NOW(), NOW());

-- =====================
-- 3. SurveyOption 더미 데이터
-- =====================
INSERT INTO survey_option (id, item_id, value, description, co2, created_at, updated_at) VALUES
                                                                                             (1, 1, 1, '모두 잘 했다.', 0.05, NOW(), NOW()),
                                                                                             (2, 1, 2, '일부만 했다.', 0.02, NOW(), NOW()),
                                                                                             (3, 1, 3, '하지 못 했다.', 0, NOW(), NOW()),

                                                                                             (4, 2, 1, '0회', 0.24, NOW(), NOW()),
                                                                                             (5, 2, 2, '1~2회', 0.12, NOW(), NOW()),
                                                                                             (6, 2, 3, '3회 이상', 0, NOW(), NOW()),

                                                                                             (7, 3, 1, '0장', 0.15, NOW(), NOW()),
                                                                                             (8, 3, 2, '1~2장', 0.05, NOW(), NOW()),
                                                                                             (9, 3, 3, '3장 이상', 0, NOW(), NOW()),

                                                                                             (10, 4, 1, '예', 0, NOW(), NOW()),
                                                                                             (11, 4, 2, '아니오', 0.002, NOW(), NOW()),

                                                                                             (12, 5, 1, '모두 껐다.', 0.05, NOW(), NOW()),
                                                                                             (13, 5, 2, '일부만 껐다.', 0.03, NOW(), NOW()),
                                                                                             (14, 5, 3, '꺼두지 않았다.', 0, NOW(), NOW()),

                                                                                             (15, 6, 1, '뜻깊은 실천을 했다.', 0, NOW(), NOW()),
                                                                                             (16, 6, 2, '조금 실천했다.', 0, NOW(), NOW()),
                                                                                             (17, 6, 3, '하지 않았다.', 0, NOW(), NOW());