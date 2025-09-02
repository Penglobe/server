-- =====================
-- 기존 데이터 초기화
-- =====================
DELETE FROM users;
--DELETE FROM survey_option;
--DELETE FROM survey_item;


-- =====================
-- 1. User 더미 데이터
-- =====================
INSERT INTO users (type, email, password_hash, nickname, home_region_id, total_point, profile_id, kakao_id, is_profile_complete, created_at, updated_at)
VALUES
    ('USER', 'user1@example.com', 'hashed_pw1', 'Alice', 101, 100, NULL, NULL, true, NOW(), NOW()),
    ('USER', 'user2@example.com', 'hashed_pw2', 'Bob', 102, 50, NULL, NULL, false, NOW(), NOW());



-- =====================
-- 2. 설문조사 질문 더미 데이터
-- =====================
-- INSERT INTO survey_item (code, question, created_at, updated_at) VALUES
--                                                                      ('분리배출', '오늘 재활용품(종이, 플라스틱, 캔 등)을 올바르게 분리배출 했나요?', NOW(), NOW()),
--                                                                      ('일회용품', '오늘 일회용품을 얼마나 사용했나요?', NOW(), NOW()),
--                                                                      ('종이 타월 사용', '오늘 종이 타월을 얼마나 사용했나요?', NOW(), NOW()),
--                                                                      ('음식물 쓰레기', '오늘 음식물 쓰레기를 줄였나요?', NOW(), NOW()),
--                                                                      ('에너지 절약', '오늘 불필요한 전기를 끄거나 절약했나요?', NOW(), NOW()),
--                                                                      ('새로운 시도', '오늘 환경을 위한 새로운 시도를 했나요?', NOW(), NOW());

-- =====================
-- 3. 설문조사 응답별 탄소배출량 더미 데이터
-- -- =====================
-- INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
--                                                                               (37, 1, 0.05, NOW(), NOW()),
--                                                                               (37, 2, 0.02, NOW(), NOW()),
--                                                                               (37, 3, 0, NOW(), NOW());
--
-- -- 일회용품
-- INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
--                                                                               (38, 1, 0.24, NOW(), NOW()),
--                                                                               (38, 2, 0.12, NOW(), NOW()),
--                                                                               (38, 3, 0, NOW(), NOW());
--
-- -- 종이 타월 사용
-- INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
--                                                                               (39, 1, 0.15, NOW(), NOW()),
--                                                                               (39, 2, 0.05, NOW(), NOW()),
--                                                                               (39, 3, 0, NOW(), NOW());
--
-- -- 음식물 쓰레기
-- INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
--                                                                               (40, 1, 0, NOW(), NOW()),
--                                                                               (40, 2, 0.02, NOW(), NOW());
--
-- -- 에너지 절약
-- INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
--                                                                               (41, 1, 0.05, NOW(), NOW()),
--                                                                               (41, 2, 0.05, NOW(), NOW()),
--                                                                               (41, 3, 0, NOW(), NOW());
--
-- -- 새로운 시도
-- INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
--                                                                               (42, 1, 0, NOW(), NOW()),
--                                                                               (42, 2, 0, NOW(), NOW()),
--                                                                               (42, 3, 0, NOW(), NOW());
--
