select * from users;
-- ======================
-- 1. User 더미 데이터
-- =====================
-- 엔티티에 @CreationTimestamp가 있어도, Hibernate가 엔티티를 통해 INSERT할 때만 자동으로 채워짐.
-- data.sql은 Hibernate/JPA를 거치지 않고 DB에 직접 실행되기 때문에 created_at, updated_at 안주면 null로 들어가 버림
--
-- INSERT INTO users (type, email, password_hash, nickname, region_id, total_point, profile_id, kakao_id, is_profile_complete, last_week_rank, created_at, updated_at)
-- VALUES ('USER', 'user1@example.com', 'hashed_pw1', 'Alice', 101, 100, NULL, NULL, 1, NULL, NOW(), NOW());
--
-- INSERT INTO users (type, email, password_hash, nickname, region_id, total_point, profile_id, kakao_id, is_profile_complete, last_week_rank, created_at, updated_at)
-- VALUES ('USER', 'user2@example.com', 'hashed_pw2', 'Bob', 102, 50, NULL, NULL, 0, NULL, NOW(), NOW());
--
-- -- -- ======================
-- -- 2. UserCounters 더미 데이터
-- -- ======================
-- --user_id는 users 테이블의 PK(auto_increment)와 동일하게 맞춰야 함
-- --total_* 값은 모두 기본 0으로 시작
-- INSERT INTO user_counters (user_id, total_distance_co2kg, total_diet_co2kg, total_survey_co2kg,
--                            attendance_total_days, attendance_streak_days, attendance_month_days,
--                            last_attendance_date, longest_attendance_streak,
--                            created_at, updated_at)
-- VALUES
--     (1, 0, 0, 0, 0, 0, 0, NULL, 0, NOW(), NOW()), -- Alice용 카운터
--     (2, 0, 0, 0, 0, 0, 0, NULL, 0, NOW(), NOW()); -- Bob용 카운터
--
-- -- -- -- =====================
-- -- -- 3. 설문조사 질문 더미 데이터
-- -- -- =====================
-- delete from survey_item;
-- INSERT INTO survey_item (code, question, created_at, updated_at) VALUES
--                                                                      ('분리배출', '오늘 재활용품(종이, 플라스틱, 캔 등)을 올바르게 분리배출 했나요?', NOW(), NOW()),
--                                                                      ('일회용품', '오늘 일회용품을 얼마나 사용했나요?', NOW(), NOW()),
--                                                                      ('종이 타월 사용', '오늘 종이 타월을 얼마나 사용했나요?', NOW(), NOW()),
--                                                                      ('음식물 쓰레기', '오늘 음식물 쓰레기를 줄였나요?', NOW(), NOW()),
--                                                                      ('에너지 절약', '오늘 불필요한 전기를 끄거나 절약했나요?', NOW(), NOW()),
--                                                                      ('새로운 시도', '오늘 환경을 위한 새로운 시도를 했나요?', NOW(), NOW());
--
-- -- =====================
-- -- 4. 설문조사 응답별 탄소배출량 더미 데이터
-- -- -- =====================
delete from survey_option;
INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
                                                                              (1, '모두 잘 했다', 0.05, NOW(), NOW()),
                                                                              (1, '일부만 했다', 0.02, NOW(), NOW()),
                                                                              (1, '하지 못 했다', 0, NOW(), NOW());

-- 일회용품
INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
                                                                              (2, '0회', 0.24, NOW(), NOW()),
                                                                              (2, '1~2회', 0.12, NOW(), NOW()),
                                                                              (2, '3회 이상', 0, NOW(), NOW());

-- 종이 타월 사용
INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
                                                                              (3, '0장 사용', 0.15, NOW(), NOW()),
                                                                              (3, '1~2장 사용', 0.05, NOW(), NOW()),
                                                                              (3,
                                                                               '3장 이상 사용', 0, NOW(), NOW());

-- 음식물 쓰레기
INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
                                                                              (4, '예', 0, NOW(), NOW()),
                                                                              (4, '아니오', 0.02, NOW(), NOW());

-- 에너지 절약
INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
                                                                              (5, '모두 껐다', 0.05, NOW(), NOW()),
                                                                              (5, '일부만 껐다', 0.05, NOW(), NOW()),
                                                                              (5, '꺼두지 않았다', 0, NOW(), NOW());

-- 새로운 시도
INSERT INTO survey_option (survey_item_id, value, co2kg, created_at, updated_at) VALUES
                                                                              (6, '뜻깊은 실천을 했다', 0, NOW(), NOW()),
                                                                              (6,
                                                                            '조금 실천했다', 0, NOW(), NOW()),
                                                                              (6, '하지 않았다', 0, NOW(), NOW());

delete from quiz;
ALTER TABLE quiz AUTO_INCREMENT = 1;
INSERT INTO quiz (question, is_answer_true, created_at, updated_at) VALUES
                                                ('태양은 동쪽에서 뜬다.', TRUE, NOW(),NOW()),
                                                ('물은 100도에서 끓는다.', TRUE,NOW(),NOW()),
                                                ('사자는 초식동물이다.', FALSE, NOW(),NOW()),
                                                ('지구는 평평하다.', FALSE, NOW(),NOW()),
                                                ('컴퓨터는 계산기와 같다.', TRUE, NOW(),NOW()),
                                                ('바나나는 나무에서 자란다.', FALSE, NOW(),NOW()),
                                                ('달은 스스로 빛을 낸다.', FALSE, NOW(),NOW()),
                                                ('인체의 뼈는 206개이다.', TRUE, NOW(),NOW()),
                                                ('전자레인지로 금속을 데울 수 있다.', FALSE, NOW(),NOW()),
                                                ('코끼리는 포유류이다.', TRUE, NOW(),NOW());