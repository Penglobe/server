# 🐧 Penglobe Server

Penglobe 프로젝트의 Spring Boot 기반 서버입니다. 사용자 활동을 통해 탄소 절감량을 측정하고, 보상을 제공하며, 랭킹 시스템을 통해 경쟁과 참여를 유도하는 백엔드 시스템입니다.

## ✨ 주요 기능

- **사용자 관리**: 자체 회원가입/로그인 및 소셜 로그인(Kakao) 기능
- **탄소 절감 활동 기록**:
    - **식단**: 채식 위주 식단 기록을 통한 탄소 절감량 계산 (LLM 활용)
    - **교통**: 도보, 자전거, 대중교통 이용 기록 및 탄소 절감량 계산
- **보상 시스템**:
    - **포인트**: 탄소 절감 활동, 퀴즈, 출석체크 등을 통해 포인트 지급
    - **결제**: 아임포트(PortOne) 연동을 통한 포인트 충전
    - **상점**: 포인트로 상품을 구매하는 기능
- **랭킹 시스템**: 주간/전체/지역별 탄소 절감량 랭킹 제공
- **기타**: 오늘의 퀴즈, 미션, 출석체크 등 사용자 참여 유도 기능

## 🛠️ 기술 스택

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: MariaDB
- **Authentication**: Spring Security, JWT, OAuth2
- **API**: REST API, Springdoc (Swagger-UI)
- **ORM**: Spring Data JPA
- **Payment**: 아임포트 (PortOne)
- **AI**: Groq LLM
- **Build Tool**: Gradle

## 📜 ERD

<img width="2250" height="1727" alt="Penglobe_ERD" src="https://github.com/user-attachments/assets/ddf444d2-2590-4552-bb3e-e8ea7d31da86" />


## 🗄️ 데이터베이스 주요 엔티티

- **User**: 사용자 정보 (이메일, 닉네임, 소셜 정보 등)
- **TransportActivity**: 교통수단(도보, 자전거, 대중교통) 이용 기록
- **Diet**: 식단 기록 및 탄소 절감량
- **PointsLedger**: 사용자 포인트 획득/사용 내역
- **Order / Product**: 상점 상품 및 주문 정보
- **Ranking**: 주간/전체 랭킹 정보
- **Attendance**: 출석체크 기록
- **Quiz / Mission**: 퀴즈 및 미션 정보

## ⚙️ 환경 변수 설정 (.properties)

서버를 실행하기 전에 `src/main/resources/application.properties` 파일을 생성하고, 아래와 같은 주요 정보를 환경에 맞게 설정해야 합니다.

```properties
# Database
spring.datasource.url=jdbc:mariadb://<DB_HOST>:<DB_PORT>/<DB_NAME>
spring.datasource.username=<DB_USERNAME>
spring.datasource.password=<DB_PASSWORD>

# JWT
jwt.secret=<YOUR_JWT_SECRET_KEY>

# Kakao OAuth2
spring.security.oauth2.client.registration.kakao.client-id=<KAKAO_REST_API_KEY>
spring.security.oauth2.client.registration.kakao.client-secret=<KAKAO_CLIENT_SECRET>

# PortOne (I'mport)
portone.api-key=<PORTONE_API_KEY>
portone.api-secret=<PORTONE_API_SECRET>

# Groq AI
groq.api-key=<GROQ_API_KEY>
```

## ▶️ 실행 방법

1. **프로젝트 클론**
   ```bash
   git clone https://github.com/your-repo/penglobe.git
   cd penglobe/server
   ```

2. **환경 변수 설정**
   위의 `환경 변수 설정` 섹션을 참고하여 `application.properties` 파일을 설정합니다.

3. **빌드**
   ```bash
   ./gradlew build
   ```

4. **실행**
   ```bash
   java -jar build/libs/server-0.0.1-SNAPSHOT.jar
   ```

## 📖 API 문서

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

서버 실행 후 위 주소로 접속하면 모든 API의 명세와 테스트를 직접 수행할 수 있습니다.
