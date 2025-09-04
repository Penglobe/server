-- ======================
-- 1. User 더미 데이터
-- =====================
-- 엔티티에 @CreationTimestamp가 있어도, Hibernate가 엔티티를 통해 INSERT할 때만 자동으로 채워짐.
-- data.sql은 Hibernate/JPA를 거치지 않고 DB에 직접 실행되기 때문에 created_at, updated_at 안주면 null로 들어가 버림
-- INSERT INTO users (type, email, password_hash, nickname, region_id, total_point, profile_id, kakao_id, is_profile_complete, last_week_rank, created_at, updated_at)
-- VALUES ('USER', 'user1@example.com', 'hashed_pw1', 'Alice', 101, 100, NULL, NULL, 1, NULL, NOW(), NOW());
--
-- INSERT INTO users (type, email, password_hash, nickname, region_id, total_point, profile_id, kakao_id, is_profile_complete, last_week_rank, created_at, updated_at)
-- VALUES ('USER', 'user2@example.com', 'hashed_pw2', 'Bob', 102, 50, NULL, NULL, 0, NULL, NOW(), NOW());

-- ======================
-- 2. UserCounters 더미 데이터
-- ======================
-- user_id는 users 테이블의 PK(auto_increment)와 동일하게
-- total_* 값은 모두 기본 0으로 시작
-- INSERT INTO user_counters (user_id, total_distance_co2kg, total_diet_co2kg, total_survey_co2kg,
--                            attendance_total_days, attendance_streak_days, attendance_month_days,
--                            last_attendance_date, longest_attendance_streak,
--                            created_at, updated_at)
-- VALUES
--     (1, 0, 0, 0, 0, 0, 0, NULL, 0, NOW(), NOW()), -- Alice용 카운터
--     (2, 0, 0, 0, 0, 0, 0, NULL, 0, NOW(), NOW()); -- Bob용 카운터

-- ======================================================
-- ======== 새로운 테스트 사용자 및 활동 데이터 추가 ========
-- ======================================================

-- 15명의 새로운 사용자 추가 (ID: 100 ~ 114)
INSERT INTO users (user_id, type, email, password_hash, nickname, region_id, total_point, is_profile_complete, last_week_rank, created_at, updated_at) VALUES
                                                                                                                                                           (100, 'USER', 'user100@example.com', 'pw100', '에코워리어', 1, 100, 1, 5, NOW(), NOW()),
                                                                                                                                                           (101, 'USER', 'user101@example.com', 'pw101', '탄소사냥꾼', 2, 50, 0, 12, NOW(), NOW()),
                                                                                                                                                           (102, 'USER', 'user102@example.com', 'pw102', '지구지킴이', 3, 200, 1, 3, NOW(), NOW()),
                                                                                                                                                           (103, 'USER', 'user103@example.com', 'pw103', '펭귄친구', 4, 150, 1, 8, NOW(), NOW()),
                                                                                                                                                           (104, 'USER', 'user104@example.com', 'pw104', '나무사랑', 5, 80, 0, 15, NOW(), NOW()),
                                                                                                                                                           (105, 'USER', 'user105@example.com', 'pw105', '재활용마스터', 6, 120, 1, 7, NOW(), NOW()),
                                                                                                                                                           (106, 'USER', 'user106@example.com', 'pw106', '녹색발자국', 7, 90, 1, 11, NOW(), NOW()),
                                                                                                                                                           (107, 'USER', 'user107@example.com', 'pw107', '환경수호자', 8, 180, 0, 4, NOW(), NOW()),
                                                                                                                                                           (108, 'USER', 'user108@example.com', 'pw108', '클린에너지', 9, 70, 1, 14, NOW(), NOW()),
                                                                                                                                                           (109, 'USER', 'user109@example.com', 'pw109', '자연보호가', 10, 110, 1, 9, NOW(), NOW()),
                                                                                                                                                           (110, 'USER', 'user110@example.com', 'pw110', '푸른하늘', 11, 220, 0, 2, NOW(), NOW()),
                                                                                                                                                           (111, 'USER', 'user111@example.com', 'pw111', '바다지기', 12, 60, 1, 16, NOW(), NOW()),
                                                                                                                                                           (112, 'USER', 'user112@example.com', 'pw112', '숲속친구', 13, 250, 1, 1, NOW(), NOW()),
                                                                                                                                                           (113, 'USER', 'user113@example.com', 'pw113', '에코라이더', 14, 130, 0, 6, NOW(), NOW()),
                                                                                                                                                           (114, 'USER', 'user114@example.com', 'pw114', '나의아이디', 15, 10, 1, 10, NOW(), NOW());

-- 새로운 사용자를 위한 UserCounters 데이터 추가
INSERT INTO user_counters (user_id, total_distance_co2kg, total_diet_co2kg, total_survey_co2kg, attendance_total_days, attendance_streak_days, attendance_month_days, last_attendance_date, longest_attendance_streak, created_at, updated_at) VALUES
                                                                                                                                                                                                                                                   (100, 10.5, 5.2, 2.1, 10, 5, 10, CURDATE(), 8, NOW(), NOW()),
                                                                                                                                                                                                                                                   (101, 8.2, 3.1, 1.5, 8, 3, 8, CURDATE(), 5, NOW(), NOW()),
                                                                                                                                                                                                                                                   (102, 15.0, 8.0, 3.0, 15, 10, 15, CURDATE(), 12, NOW(), NOW()),
                                                                                                                                                                                                                                                   (103, 12.3, 6.5, 2.5, 12, 8, 12, CURDATE(), 10, NOW(), NOW()),
                                                                                                                                                                                                                                                   (104, 5.0, 2.0, 1.0, 5, 2, 5, CURDATE(), 4, NOW(), NOW()),
                                                                                                                                                                                                                                                   (105, 9.8, 4.5, 1.8, 9, 4, 9, CURDATE(), 6, NOW(), NOW()),
                                                                                                                                                                                                                                                   (106, 7.5, 3.8, 1.2, 7, 3, 7, CURDATE(), 5, NOW(), NOW()),
                                                                                                                                                                                                                                                   (107, 18.2, 9.5, 3.5, 18, 12, 18, CURDATE(), 15, NOW(), NOW()),
                                                                                                                                                                                                                                                   (108, 4.5, 1.8, 0.8, 4, 1, 4, CURDATE(), 3, NOW(), NOW()),
                                                                                                                                                                                                                                                   (109, 9.0, 4.0, 1.6, 9, 4, 9, CURDATE(), 7, NOW(), NOW()),
                                                                                                                                                                                                                                                   (110, 20.0, 10.0, 4.0, 20, 15, 20, CURDATE(), 18, NOW(), NOW()),
                                                                                                                                                                                                                                                   (111, 3.2, 1.5, 0.5, 3, 1, 3, CURDATE(), 2, NOW(), NOW()),
                                                                                                                                                                                                                                                   (112, 25.0, 12.0, 5.0, 25, 20, 25, CURDATE(), 22, NOW(), NOW()),
                                                                                                                                                                                                                                                   (113, 11.5, 5.8, 2.3, 11, 6, 11, CURDATE(), 9, NOW(), NOW()),
                                                                                                                                                                                                                                                   (114, 1.0, 0.5, 0.2, 1, 1, 1, CURDATE(), 1, NOW(), NOW());

-- 주간 랭킹 점수 계산을 위한 활동 데이터 추가 (최근 날짜로)
INSERT INTO transport_activities (user_id, mode, start_time, end_time, distanceM, co2kg, created_at, updated_at) VALUES
                                                                                                                     (100, 'WALK', NOW() - INTERVAL 1 DAY, NOW(), 10000, 1.2, NOW() - INTERVAL 1 DAY, NOW()),
                                                                                                                     (102, 'WALK', NOW() - INTERVAL 2 DAY, NOW(), 25000, 3.0, NOW() - INTERVAL 2 DAY, NOW()),
                                                                                                                     (107, 'WALK', NOW() - INTERVAL 1 DAY, NOW(), 30000, 3.6, NOW() - INTERVAL 1 DAY, NOW()),
                                                                                                                     (110, 'WALK', NOW() - INTERVAL 3 DAY, NOW(), 40000, 4.8, NOW() - INTERVAL 3 DAY, NOW()),
                                                                                                                     (112, 'WALK', NOW() - INTERVAL 2 DAY, NOW(), 50000, 6.0, NOW() - INTERVAL 2 DAY, NOW()),
                                                                                                                     (101, 'WALK', NOW() - INTERVAL 1 DAY, NOW(), 5000, 0.6, NOW() - INTERVAL 1 DAY, NOW()),
                                                                                                                     (114, 'WALK', NOW() - INTERVAL 1 DAY, NOW(), 2000, 0.24, NOW() - INTERVAL 1 DAY, NOW());

INSERT INTO diet_records (user_id, image_url, analysis_json, co2_kg, created_at, updated_at) VALUES
                                                                                                 (100, 'http://example.com/salad.jpg', '{}', 0.8, NOW() - INTERVAL 1 DAY, NOW()),
                                                                                                 (102, 'http://example.com/burger.jpg', '{}', 1.5, NOW() - INTERVAL 2 DAY, NOW()),
                                                                                                 (107, 'http://example.com/steak.jpg', '{}', 1.8, NOW() - INTERVAL 1 DAY, NOW()),
                                                                                                 (110, 'http://example.com/risotto.jpg', '{}', 2.2, NOW() - INTERVAL 3 DAY, NOW()),
                                                                                                 (112, 'http://example.com/stew.jpg', '{}', 2.5, NOW() - INTERVAL 2 DAY, NOW()),
                                                                                                 (104, 'http://example.com/fruit.jpg', '{}', 0.5, NOW() - INTERVAL 1 DAY, NOW()),
                                                                                                 (114, 'http://example.com/apple.jpg', '{}', 0.1, NOW() - INTERVAL 1 DAY, NOW());

INSERT INTO survey_response (user_id, total_co2kg, created_at, updated_at) VALUES
                                                                               (100, 0.05, NOW() - INTERVAL 1 DAY, NOW()),
                                                                               (102, 0.24, NOW() - INTERVAL 2 DAY, NOW()),
                                                                               (107, 0.15, NOW() - INTERVAL 1 DAY, NOW()),
                                                                               (110, 0.05, NOW() - INTERVAL 3 DAY, NOW()),
                                                                               (112, 0.24, NOW() - INTERVAL 2 DAY, NOW()),
                                                                               (114, 0, NOW() - INTERVAL 1 DAY, NOW());
/*
DELETE FROM regions;
ALTER TABLE regions AUTO_INCREMENT = 1;*/

INSERT INTO regions (name, total_co2kg) VALUES
                                            ('서울특별시', 0),
                                            ('부산광역시', 0),
                                            ('대구광역시', 0),
                                            ('인천광역시', 0),
                                            ('광주광역시', 0),
                                            ('대전광역시', 0),
                                            ('울산광역시', 0),
                                            ('세종특별자치시', 0),
                                            ('경기도', 0),
                                            ('강원도', 0),
                                            ('충청북도', 0),
                                            ('충청남도', 0),
                                            ('전라북도', 0),
                                            ('전라남도', 0),
                                            ('경상북도', 0),
                                            ('경상남도', 0),
                                            ('제주특별자치도', 0);