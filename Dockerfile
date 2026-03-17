# ── 1단계: 빌드 ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Gradle Wrapper와 빌드 설정 먼저 복사 (레이어 캐시 활용)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# 소스 복사 후 빌드 (테스트 제외)
COPY src/ src/

RUN ./gradlew bootJar -x test --no-daemon

# ── 2단계: 실행 ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 업로드 디렉터리 생성
RUN mkdir -p /home/student/penglobe/uploads

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# docker 프로필 활성화 (application-docker.properties 사용)
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
