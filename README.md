# common-jdk25

> Spring Boot 3.5 + Java 25 기반 엔터프라이즈 공통 모듈 및 프로젝트 템플릿

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-Wrapper-blue.svg)](https://gradle.org/)

## 목차

- [개요](#개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [빠른 시작](#빠른-시작)
- [환경 설정](#환경-설정)
- [핵심 기능 상세](#핵심-기능-상세)
- [API 문서](#api-문서)
- [코딩 컨벤션](#코딩-컨벤션)
- [트러블슈팅](#트러블슈팅)
- [라이선스](#라이선스)

---

## 개요

`common-jdk25`는 최신 Java 25와 Spring Boot 3.5 기반의 엔터프라이즈급 공통 모듈 프로젝트입니다.
신규 서비스 개발 시 템플릿으로 활용하거나, 팀 내 공통 모듈로 사용할 수 있도록 설계되었습니다.

### 특징

- **최신 기술 스택**: Java 25, Spring Boot 3.5.7
- **보안 강화**: JWT 기반 인증, Spring Security 통합
- **고가용성**: Resilience4j 기반 Circuit Breaker & Retry
- **API 표준화**: 일관된 응답 포맷, OpenAPI 문서 자동 생성
- **관측성(Observability)**: 구조화된 로깅, MDC 기반 추적, Micrometer OTel 통합
- **확장성**: Feature Flag 패턴, 비동기 처리, Rate Limiting, 멱등성 보장
- **68개 Java 클래스**: 즉시 사용 가능한 풍부한 유틸리티 및 설정

---

## 주요 기능

### 보안 & 인증
- JWT 기반 인증/인가 (Access Token, Refresh Token)
- Spring Security + JWT Filter Chain
- OAuth 2.0 Client Credentials Grant Type 지원
- CORS, CSRF 설정 완료
- BCrypt 비밀번호 암호화

### API & 통신
- RESTful API 표준 응답 포맷 (`BaseResponse<T>`)
- Apache HttpClient5 기반 RestClient 구성
- Resilience4j Circuit Breaker & Retry 적용
- 요청/응답 로깅 및 민감정보 마스킹
- OpenAPI 3.0 문서 자동 생성 (Swagger UI)

### 안정성 & 성능
- **Rate Limiting**: IP 기반 요청 제한 (기본: 100req/60sec)
- **Idempotency**: 멱등성 보장 (Idempotency-Key 헤더)
- **Circuit Breaker**: 외부 API 장애 격리 (실패율 50% 임계치)
- **Retry**: 자동 재시도 (최대 3회, 200ms 간격)
- **비동기 처리**: @Async 지원, MDC 컨텍스트 전파

### 개발 생산성
- 통합 예외 처리 (`GlobalExceptionHandler`)
- 페이징 공통 모델 (`PageRequestDto`, `PageResponseDto`)
- 6개 유틸리티 클래스 (JSON, 날짜, 마스킹, 네트워크 등)
- 커스텀 어노테이션 (`@ValidEnum`)
- 환경별 프로파일 분리 (local, dev, prod)
- Feature Flag 기반 선택적 기능 활성화

---

## 기술 스택

| 카테고리 | 기술 | 버전 | 비고 |
|---------|------|------|------|
| **언어** | Java | 25 | JDK 25 Toolchain |
| **프레임워크** | Spring Boot | 3.5.7 | 최신 안정 버전 |
| | Spring Web | - | RESTful API |
| | Spring Security | 6.x | JWT 인증 |
| | Spring Data JPA | - | ORM (선택적) |
| | Spring Actuator | - | 헬스체크, 메트릭 |
| **보안** | JJWT | 0.12.6 | JWT 생성/검증 |
| **회복성** | Resilience4j | 2.3.0 | Circuit Breaker, Retry |
| **HTTP 클라이언트** | Apache HttpClient5 | - | RestClient 백엔드 |
| **문서화** | springdoc-openapi | 2.8.14 | OpenAPI 3.0, Swagger UI |
| **로깅** | Logback | 1.x | 구조화된 로깅 |
| | datasource-proxy | 1.12.0 | SQL 로깅 |
| **추적** | Micrometer OTel | - | 분산 추적 |
| **유틸리티** | Apache Commons Lang3 | 3.20.0 | 공통 유틸 |
| | Lombok | Latest | 보일러플레이트 제거 |
| **빌드** | Gradle | Wrapper | 빌드 자동화 |
| **DB** | H2 | Latest | 로컬 개발용 |
| **테스트** | JUnit 5 | Latest | 단위/통합 테스트 |

---

## 프로젝트 구조

```
common-jdk25/
├── build.gradle                    # Gradle 빌드 설정
├── settings.gradle
├── lombok.config                   # Lombok 설정
├── logs/                          # 로그 파일 생성 경로
└── src/
    ├── main/
    │   ├── java/mingovvv/common/
    │   │   ├── CommonJdk25Application.java     # 메인 클래스
    │   │   │
    │   │   ├── annotation/                     # 커스텀 어노테이션 (2개)
    │   │   │   ├── ValidEnum.java              # Enum 검증 어노테이션
    │   │   │   └── EnumValidator.java          # Enum 검증 로직
    │   │   │
    │   │   ├── async/                          # 비동기 처리 (3개)
    │   │   │   ├── config/
    │   │   │   │   ├── AsyncConfig.java        # @Async 설정
    │   │   │   │   └── AsyncProperties.java
    │   │   │   └── impl/
    │   │   │       └── MdcTaskDecorator.java   # MDC 전파
    │   │   │
    │   │   ├── config/                         # 애플리케이션 설정 (5개)
    │   │   │   ├── JacksonConfig.java          # ObjectMapper 설정
    │   │   │   ├── JpaAuditingConfig.java      # JPA Auditing
    │   │   │   ├── OpenApiConfig.java          # Swagger 설정
    │   │   │   └── SecurityConfig.java         # Spring Security + JWT
    │   │   │
    │   │   ├── constants/                      # 상수 (2개)
    │   │   │   ├── ResultCode.java             # API 응답 코드
    │   │   │   └── ResultType.java             # 응답 타입
    │   │   │
    │   │   ├── exception/                      # 예외 클래스 (4개)
    │   │   │   ├── GlobalException.java        # 최상위 예외
    │   │   │   ├── BusinessException.java      # 비즈니스 로직 예외
    │   │   │   ├── ExternalApiException.java   # 외부 API 예외
    │   │   │   └── InternalServerException.java
    │   │   │
    │   │   ├── filter/                         # HTTP 필터 (3개)
    │   │   │   ├── AccessLogFilter.java        # 요청/응답 로그
    │   │   │   ├── ExceptionHandlerFilter.java # 필터 레벨 예외 처리
    │   │   │   └── wrapper/
    │   │   │       └── CustomRequestWrapper.java
    │   │   │
    │   │   ├── handler/                        # 예외 핸들러 (1개)
    │   │   │   └── GlobalExceptionHandler.java # 전역 예외 처리
    │   │   │
    │   │   ├── http/                           # HTTP 클라이언트 (10개)
    │   │   │   ├── client/
    │   │   │   │   └── TestServerClient.java   # 예제 클라이언트
    │   │   │   ├── config/
    │   │   │   │   ├── RestClientConfig.java
    │   │   │   │   ├── RestClientBuilder.java
    │   │   │   │   ├── Resilience4jRestClientInterceptorFactory.java
    │   │   │   │   └── Resilience4jRestClientProperties.java
    │   │   │   ├── dto/
    │   │   │   │   ├── TestServerReq.java
    │   │   │   │   └── TestServerRes.java
    │   │   │   └── interceptor/
    │   │   │       ├── RestClientLoggingInterceptor.java
    │   │   │       └── Resilience4jRestClientInterceptor.java
    │   │   │
    │   │   ├── idempotency/                    # 멱등성 보장 (7개)
    │   │   │   ├── config/
    │   │   │   ├── filter/
    │   │   │   ├── impl/
    │   │   │   ├── model/
    │   │   │   └── IdempotencyStore.java
    │   │   │
    │   │   ├── mockapi/                        # Mock API (1개)
    │   │   │   └── MockApiController.java
    │   │   │
    │   │   ├── model/                          # 공통 모델 (5개)
    │   │   │   ├── BaseResponse.java           # 표준 응답
    │   │   │   ├── BaseResponseFactory.java
    │   │   │   ├── PageInfo.java
    │   │   │   ├── PageRequestDto.java
    │   │   │   └── PageResponseDto.java
    │   │   │
    │   │   ├── oauth/                          # OAuth 2.0 (7개)
    │   │   │   ├── controller/
    │   │   │   │   └── OAuth2TokenController.java
    │   │   │   ├── domain/
    │   │   │   │   └── OAuth2Client.java
    │   │   │   ├── dto/
    │   │   │   │   ├── OAuth2TokenRequest.java
    │   │   │   │   └── OAuth2TokenResponse.java
    │   │   │   ├── exception/
    │   │   │   ├── repository/
    │   │   │   └── service/
    │   │   │       └── OAuth2TokenService.java
    │   │   │
    │   │   ├── properties/                     # 프로퍼티 (1개)
    │   │   │   └── JwtProperties.java
    │   │   │
    │   │   ├── ratelimit/                      # Rate Limiting (8개)
    │   │   │   ├── config/
    │   │   │   ├── filter/
    │   │   │   ├── impl/
    │   │   │   ├── model/
    │   │   │   ├── RateLimiter.java
    │   │   │   └── RateLimitKeyResolver.java
    │   │   │
    │   │   ├── security/                       # JWT 보안 (5개)
    │   │   │   ├── JwtTokenProvider.java       # JWT 생성/검증
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── JwtAuthenticationEntryPoint.java
    │   │   │   ├── JwtAccessDeniedHandler.java
    │   │   │   └── CustomUserDetailsService.java
    │   │   │
    │   │   └── utils/                          # 유틸리티 (6개)
    │   │       ├── DateUtil.java               # 날짜/시간
    │   │       ├── JsonUtil.java               # JSON 직렬화
    │   │       ├── MaskingUtil.java            # 민감정보 마스킹
    │   │       ├── MDCUtil.java                # MDC 키 관리
    │   │       ├── NetworkUtil.java            # IP, 헤더 처리
    │   │       └── StringUtil.java             # 문자열 처리
    │   │
    │   └── resources/
    │       ├── application.yml                 # 기본 설정
    │       ├── application-local.yml           # 로컬 환경
    │       ├── application-dev.yml             # 개발 환경
    │       ├── application-prod.yml            # 운영 환경
    │       └── logback-spring.xml              # 로깅 설정
    │
    └── test/
        └── java/mingovvv/common/
            └── CommonJdk25ApplicationTests.java
```

**총 68개 Java 파일** (annotation: 2, async: 3, config: 5, exception: 4, filter: 3, handler: 1, http: 10, idempotency: 7, oauth: 7, ratelimit: 8, security: 5, utils: 6, model: 5, 기타: 2)

---

## 빠른 시작

### 사전 요구사항

- **JDK 25** 설치 ([Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 또는 [OpenJDK](https://openjdk.org/))
- **Gradle** (Wrapper 포함, 별도 설치 불필요)
- **Git** (선택사항)

### 프로젝트 클론 및 실행

```bash
# 1. 프로젝트 클론 (또는 디렉토리로 이동)
cd C:\Users\mk.jang\Desktop\TLC\common-jdk25

# 2. 의존성 다운로드 및 빌드
./gradlew.bat build

# 3. 애플리케이션 실행 (기본: local 프로파일)
./gradlew.bat bootRun

# 4. 브라우저에서 확인
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - Mock API: http://localhost:8080/mock/ping
# - Health Check: http://localhost:8080/actuator/health
```

### IDE(IntelliJ) 실행

1. IntelliJ에서 프로젝트 열기
2. `CommonJdk25Application.java` 파일 열기
3. `main` 메서드 옆 실행 버튼 클릭
4. Edit Configurations → Environment variables → `spring.profiles.active=local` 추가 (선택)

---

## 환경 설정

### 프로파일 구성

| 프로파일 | 설명 | 활성화 방법 |
|---------|------|-----------|
| **local** | 로컬 개발 (기본값) | `--spring.profiles.active=local` |
| **dev** | 개발 서버 | `--spring.profiles.active=dev` |
| **prod** | 운영 서버 | `--spring.profiles.active=prod` |

### 주요 설정 (application.yml)

```yaml
# JWT 설정
jwt:
  secret-key: ${JWT_SECRET_KEY:your-secret-key}  # 256비트 이상 필수
  access-token-validity: 3600000                 # 1시간
  refresh-token-validity: 604800000              # 7일
  issuer: common-jdk25
  header: Authorization
  prefix: Bearer

# Feature Flags (선택적 기능 활성화)
feature:
  db:
    enabled: false                  # Database 기능 (OAuth2 등)
  rate-limit:
    enabled: false                  # Rate Limiting
    max-requests: 100
    window-seconds: 60
  idempotency:
    enabled: false                  # 멱등성 보장
    header-name: Idempotency-Key
    ttl-seconds: 300
  resilience4j:
    rest-client:
      retry-enabled: true           # 자동 재시도
      circuit-breaker-enabled: true # Circuit Breaker

# 비동기 설정
async:
  core-pool-size: 4
  max-pool-size: 8
  queue-capacity: 100

# Resilience4j 설정
resilience4j:
  retry:
    instances:
      default:
        max-attempts: 3             # 최대 3회 시도
        wait-duration: 200ms
  circuitbreaker:
    instances:
      default:
        sliding-window-size: 20
        failure-rate-threshold: 50  # 50% 실패 시 OPEN
        wait-duration-in-open-state: 30s
```

### 환경 변수 설정 (권장)

**Linux/Mac:**
```bash
export JWT_SECRET_KEY="your-production-secret-key-min-256-bits"
export SPRING_PROFILES_ACTIVE=prod
```

**Windows PowerShell:**
```powershell
$env:JWT_SECRET_KEY="your-production-secret-key-min-256-bits"
$env:SPRING_PROFILES_ACTIVE="prod"
```

---

## 핵심 기능 상세

### 1. JWT 기반 인증

#### 주요 클래스
- `JwtTokenProvider`: JWT 생성/검증/파싱 (src/main/java/mingovvv/common/security/JwtTokenProvider.java)
- `JwtAuthenticationFilter`: JWT 인증 필터 (src/main/java/mingovvv/common/security/JwtAuthenticationFilter.java)
- `SecurityConfig`: Spring Security 설정 (src/main/java/mingovvv/common/config/SecurityConfig.java)

#### 사용 예제

```java
// 1. Access Token 생성
Authentication authentication = ...; // 사용자 인증 정보
String accessToken = jwtTokenProvider.generateAccessToken(authentication);

// 2. Refresh Token 생성
String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

// 3. Token 검증
boolean isValid = jwtTokenProvider.validateToken(token);

// 4. Token에서 사용자 정보 추출
String username = jwtTokenProvider.getUsernameFromToken(token);
List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);
```

#### API 호출 시 인증 헤더

```bash
curl -X GET http://localhost:8080/api/protected \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 2. OAuth 2.0 Client Credentials

#### 엔드포인트

**POST /oauth/token** - 토큰 발급

**요청 예시:**
```bash
curl -X POST http://localhost:8080/oauth/token \
  -H "Content-Type: application/json" \
  -d '{
    "grant_type": "client_credentials",
    "client_id": "your-client-id",
    "client_secret": "your-client-secret",
    "scope": "read write"
  }'
```

**응답 예시:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read write"
}
```

**주의:** DB 기능 활성화 필요 (`feature.db.enabled: true`)

### 3. OpenAPI / Swagger 문서

#### 접속 URL
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

#### 주요 기능
- JWT Bearer 인증 스키마 자동 추가
- 모든 엔드포인트에 공통 에러 응답 자동 등록 (400, 401, 403, 404, 429, 500)
- BaseResponse 스키마 자동 포함
- Try it out 기능으로 API 테스트 가능

#### 운영 환경 보안
```yaml
# application-prod.yml
springdoc:
  api-docs:
    enabled: false  # OpenAPI JSON 비활성화
  swagger-ui:
    enabled: false  # Swagger UI 비활성화
```

### 4. Resilience4j (Circuit Breaker & Retry)

#### 설정 위치
- `http/config/RestClientConfig.java`
- `http/interceptor/Resilience4jRestClientInterceptor.java`

#### 동작 방식
1. **Retry**: 외부 API 호출 실패 시 최대 3회 자동 재시도 (200ms 간격)
2. **Circuit Breaker**: 실패율 50% 초과 시 30초간 호출 차단 (OPEN 상태)
3. **Half-Open**: 5회 호출 성공 시 정상 상태로 복구 (CLOSED)

#### 클라이언트 예제

```java
@Component
public class ExternalApiClient {

    @Autowired
    private RestClient restClient;

    public ResponseDto callExternalApi() {
        // Resilience4j가 자동으로 Retry + Circuit Breaker 적용
        return restClient.get()
                .uri("/api/endpoint")
                .retrieve()
                .body(ResponseDto.class);
    }
}
```

### 5. Rate Limiting (IP 기반)

#### 설정
```yaml
feature.rate-limit:
  enabled: true
  max-requests: 100     # 60초당 최대 100개 요청
  window-seconds: 60
```

#### 동작 방식
- IP 주소별 요청 횟수 추적
- 제한 초과 시 `429 Too Many Requests` 응답
- 인메모리 구현 (분산 환경에서는 Redis 구현 권장)

#### 응답 예시
```json
{
  "status": "FAIL",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청 제한 초과. 60초 후 다시 시도하세요.",
  "traceId": "abc123"
}
```

### 6. 멱등성 보장 (Idempotency)

#### 설정
```yaml
feature.idempotency:
  enabled: true
  header-name: Idempotency-Key
  ttl-seconds: 300
  methods: [POST, PUT]
```

#### 사용 방법

**클라이언트 요청:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-12345" \
  -d '{"product_id": 1, "quantity": 2}'
```

**동작:**
1. 동일한 `Idempotency-Key`로 재요청 시 캐시된 응답 반환
2. TTL(300초) 이내에 중복 요청 방지
3. 네트워크 재시도, 클라이언트 중복 클릭 등에 안전

### 7. 비동기 처리 (@Async)

#### 설정
```java
@Configuration
@EnableAsync
public class AsyncConfig extends AsyncConfigurer {
    // ThreadPoolTaskExecutor 설정 (Core: 4, Max: 8)
    // MdcTaskDecorator로 MDC 컨텍스트 자동 전파
}
```

#### 사용 예제

```java
@Service
public class NotificationService {

    @Async
    public CompletableFuture<Void> sendEmailAsync(String to, String subject) {
        // 비동기로 이메일 전송
        emailClient.send(to, subject);
        return CompletableFuture.completedFuture(null);
    }
}
```

### 8. 표준 응답 포맷 (BaseResponse)

#### 구조

```json
{
  "status": "SUCCESS",
  "code": "S0000",
  "message": "정상 처리되었습니다.",
  "data": {
    "id": 1,
    "name": "테스트"
  },
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

#### 사용 예제

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public BaseResponse<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return BaseResponseFactory.success(ResultCode.SUCCESS, user);
    }

    @PostMapping
    public BaseResponse<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDto created = userService.create(request);
        return BaseResponseFactory.success(ResultCode.CREATED, created);
    }
}
```

### 9. 페이징 공통 모델

#### 요청 예시

```java
@GetMapping("/api/users")
public BaseResponse<PageResponseDto<UserDto>> getUsers(
    @Valid PageRequestDto pageRequest
) {
    Page<User> page = userRepository.findAll(pageRequest.toPageable());
    PageResponseDto<UserDto> response = PageResponseDto.of(page, UserDto::from);
    return BaseResponseFactory.success(ResultCode.SUCCESS, response);
}
```

#### 응답 예시

```json
{
  "status": "SUCCESS",
  "code": "S0000",
  "message": "정상 처리되었습니다.",
  "data": {
    "content": [
      {"id": 1, "name": "홍길동"},
      {"id": 2, "name": "김철수"}
    ],
    "pageInfo": {
      "page": 0,
      "size": 10,
      "totalElements": 2,
      "totalPages": 1,
      "isFirst": true,
      "isLast": true
    }
  },
  "traceId": "abc123"
}
```

### 10. 유틸리티 클래스

#### DateUtil (날짜/시간 처리)

```java
// 날짜 포매팅
String formatted = DateUtil.formatDate(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");

// 날짜 파싱
LocalDateTime parsed = DateUtil.parseDate("2024-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");

// ZonedDateTime 변환
ZonedDateTime zdt = DateUtil.toZonedDateTime(LocalDateTime.now(), "Asia/Seoul");
```

#### JsonUtil (JSON 직렬화)

```java
// 객체 → JSON 문자열
String json = JsonUtil.toJson(userDto);

// JSON → 객체
UserDto user = JsonUtil.fromJson(json, UserDto.class);

// Pretty Print
String prettyJson = JsonUtil.toPrettyJson(userDto);
```

#### MaskingUtil (민감정보 마스킹)

```java
// 이메일: hong****@example.com
String masked = MaskingUtil.maskEmail("honggildong@example.com");

// 전화번호: 010-****-5678
String masked = MaskingUtil.maskPhone("010-1234-5678");

// 이름: 홍*동
String masked = MaskingUtil.maskName("홍길동");

// 카드번호: 1234-****-****-5678
String masked = MaskingUtil.maskCardNumber("1234-5678-9012-3456");
```

#### NetworkUtil (네트워크 처리)

```java
// 클라이언트 실제 IP 조회 (X-Forwarded-For 등 고려)
String clientIp = NetworkUtil.getClientIp(request);

// 헤더 값 조회
String userAgent = NetworkUtil.getHeader(request, "User-Agent");
```

#### MDCUtil (MDC 관리)

```java
// TraceId 설정
MDCUtil.setTraceId(UUID.randomUUID().toString());

// TraceId 조회
String traceId = MDCUtil.getTraceId();

// MDC 클리어
MDCUtil.clear();
```

---

## API 문서

### 엔드포인트 목록

| 경로 | 메서드 | 설명 | 인증 | 비고 |
|------|--------|------|------|------|
| `/swagger-ui.html` | GET | Swagger UI | X | 공개 |
| `/v3/api-docs` | GET | OpenAPI JSON | X | 공개 |
| `/mock/ping` | GET | Mock API 테스트 | X | 공개 |
| `/oauth/token` | POST | OAuth 토큰 발급 | X | Client Credentials |
| `/actuator/health` | GET | 헬스체크 | X | 공개 |
| `/actuator/info` | GET | 애플리케이션 정보 | X | 공개 |
| `/actuator/metrics` | GET | 메트릭 | O | 인증 필수 |
| `/api/auth/**` | - | 인증 API | X | 공개 |
| `/api/public/**` | - | 공개 API | X | 공개 |
| `/api/**` | - | 보호된 API | O | JWT 인증 필수 |

### 에러 응답 코드

| HTTP 상태 | ResultCode | 설명 |
|-----------|------------|------|
| 400 | `E4000` | 잘못된 요청 (Validation 실패 등) |
| 401 | `E4010` | 인증 실패 (JWT 누락/만료) |
| 403 | `E4030` | 권한 부족 |
| 404 | `E4040` | 리소스 없음 |
| 429 | `E4290` | Rate Limit 초과 |
| 500 | `E5000` | 내부 서버 오류 |
| 502 | `E5020` | 외부 API 호출 실패 |
| 503 | `E5030` | 서비스 일시 중단 (Circuit Breaker OPEN) |

---

## 로깅

### 로그 파일 위치

- **일반 로그**: `logs/mingo-server.log` (전체 로그)
- **에러 로그**: `logs/mingo-server-error.log` (ERROR 레벨만)

### 로그 레벨 설정

```yaml
# application.yml
logging:
  level:
    root: INFO
    org.springframework: INFO
    mingovvv.common: DEBUG           # 개발 시 디버깅
    org.hibernate.SQL: DEBUG         # SQL 로그 (개발)
```

### 로그 포맷

```
2024-01-15 10:23:45.123 [http-nio-8080-exec-1] INFO  [traceId:abc123] [spanId:def456] c.m.c.AccessLogFilter -
[REQ] GET /api/users?page=0&size=10
[REQ] Headers: {Authorization=Bearer eyJ..., Content-Type=application/json}
[REQ] Body: (empty)

2024-01-15 10:23:45.456 [http-nio-8080-exec-1] INFO  [traceId:abc123] [spanId:def456] c.m.c.AccessLogFilter -
[RES] GET /api/users?page=0&size=10 - 200 OK (333ms)
[RES] Body: {"status":"SUCCESS","code":"S0000",...}
```

### 민감정보 마스킹

`AccessLogFilter`, `RestClientLoggingInterceptor`에서 자동으로 다음 정보 마스킹:
- 비밀번호 필드 (`password`, `pwd` 등)
- Authorization 헤더 (일부만 표시)
- 이메일, 전화번호 (MaskingUtil 사용)

---

## 코딩 컨벤션

### Java 스타일
- **패키지 네이밍**: 소문자, 단수형 (예: `controller`, `service`)
- **클래스 네이밍**: PascalCase (예: `UserController`, `JwtTokenProvider`)
- **메서드 네이밍**: camelCase, 동사로 시작 (예: `getUserById`, `createOrder`)
- **상수**: UPPER_SNAKE_CASE (예: `MAX_RETRY_COUNT`)

### Lombok 규칙
- `@RequiredArgsConstructor`로 생성자 주입 (필드 주입 지양)
- `@Slf4j`로 로거 사용
- `@Getter`, `@Setter` 신중히 사용 (DTO에만)
- `@Data` 사용 금지 (명시적으로 필요한 것만 사용)

### 계층 구조
```
Controller → Service → Repository
```
- **Controller**: HTTP 요청/응답 처리, Validation
- **Service**: 비즈니스 로직, 트랜잭션 관리
- **Repository**: 데이터 접근

### 예외 처리
- **비즈니스 규칙 위반**: `BusinessException` 사용
- **외부 API 실패**: `ExternalApiException` 사용 (원인 체인 보존)
- **내부 서버 오류**: `InternalServerException` 사용

```java
// Good
if (user == null) {
    throw new BusinessException(ResultCode.USER_NOT_FOUND);
}

// Bad
if (user == null) {
    throw new RuntimeException("사용자를 찾을 수 없습니다.");
}
```

### Git 커밋 메시지

```
feat: JWT 인증 기능 추가
fix: Rate Limiting 버그 수정 (#123)
docs: README 업데이트
refactor: RestClient 설정 리팩토링
test: UserService 단위 테스트 추가
chore: Gradle 의존성 업데이트
```

**프리픽스:**
- `feat`: 새 기능
- `fix`: 버그 수정
- `docs`: 문서 변경
- `refactor`: 리팩토링
- `test`: 테스트 추가/수정
- `chore`: 빌드/설정 변경

### 브랜치 전략

- `main`: 운영 배포 브랜치
- `develop`: 개발 통합 브랜치
- `feature/{기능명}`: 기능 개발
- `hotfix/{버그명}`: 긴급 수정

---

## 테스트

### 단위 테스트

```bash
# 전체 테스트 실행
./gradlew.bat test

# 특정 클래스만 테스트
./gradlew.bat test --tests UserServiceTest

# 테스트 결과 HTML 리포트
./gradlew.bat test --tests UserServiceTest --info
# 리포트: build/reports/tests/test/index.html
```

### 통합 테스트 예시

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 사용자_조회_성공() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").value(1));
    }
}
```

---

## 트러블슈팅

### 1. 애플리케이션 실행 불가

**증상:** `Could not find or load main class`

**해결:**
```bash
# JDK 버전 확인 (25 이상 필요)
java -version

# Gradle 빌드 재시도
./gradlew.bat clean build

# IDE 재시작 후 Gradle Refresh
```

### 2. JWT 토큰 검증 실패

**증상:** `401 Unauthorized - Invalid JWT token`

**원인:**
- Secret Key가 256비트 미만
- 토큰 만료
- 잘못된 Bearer 헤더 형식

**해결:**
```yaml
# application.yml
jwt:
  secret-key: ${JWT_SECRET_KEY:your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm}
```

```bash
# 환경 변수로 설정 (권장)
export JWT_SECRET_KEY="your-production-secret-key-min-256-bits"
```

### 3. Circuit Breaker OPEN 상태

**증상:** `503 Service Unavailable - Circuit breaker is OPEN`

**원인:** 외부 API 실패율 50% 초과

**해결:**
```yaml
# application.yml - 임계치 조정
resilience4j:
  circuitbreaker:
    instances:
      default:
        failure-rate-threshold: 70     # 50% → 70%
        wait-duration-in-open-state: 10s  # 30s → 10s
```

### 4. Rate Limit 초과

**증상:** `429 Too Many Requests`

**해결:**
```yaml
# application.yml - 제한 완화
feature.rate-limit:
  max-requests: 200                    # 100 → 200
  window-seconds: 60
```

또는 개발 환경에서 비활성화:
```yaml
feature.rate-limit:
  enabled: false
```

### 5. 멱등성 키 중복

**증상:** 동일한 응답이 계속 반환됨

**원인:** TTL 내 동일 Idempotency-Key 재사용

**해결:**
```bash
# 매 요청마다 고유한 키 생성
curl -X POST http://localhost:8080/api/orders \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{"product_id": 1}'
```

### 6. 로그 파일 미생성

**증상:** `logs/` 디렉토리에 로그 없음

**원인:** 프로파일이 `local`로 설정되어 콘솔만 출력

**해결:**
```bash
# dev 또는 prod 프로파일로 실행
./gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

### 7. CORS 오류

**증상:** 브라우저에서 `Access-Control-Allow-Origin` 오류

**해결:**
```java
// SecurityConfig.java 수정
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("https://your-domain.com"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    // ...
}
```

---

## 운영 환경 배포

### 1. 운영 환경 설정 체크리스트

- [ ] JWT Secret Key 256비트 이상 설정 (환경 변수)
- [ ] CORS 허용 도메인 제한 (`SecurityConfig.java`)
- [ ] Swagger UI 비활성화 (`springdoc.swagger-ui.enabled: false`)
- [ ] Actuator 엔드포인트 제한 (`management.endpoints.web.exposure.include: health,info`)
- [ ] 로그 레벨 INFO 이상 (`logging.level.root: INFO`)
- [ ] DB 연결 정보 환경 변수화
- [ ] Rate Limiting, Idempotency 활성화 검토

### 2. 빌드 및 배포

```bash
# 1. JAR 빌드
./gradlew.bat build -x test

# 2. 빌드 결과물 확인
ls build/libs/common-jdk25-0.0.1-SNAPSHOT.jar

# 3. 운영 서버 실행
java -jar build/libs/common-jdk25-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  -Djwt.secret-key=${JWT_SECRET_KEY}
```

### 3. Docker 배포 (예시)

```dockerfile
# Dockerfile
FROM openjdk:25-jdk-slim
WORKDIR /app
COPY build/libs/common-jdk25-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 빌드 및 실행
docker build -t common-jdk25:latest .
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET_KEY=your-secret-key \
  common-jdk25:latest
```

### 4. 모니터링

#### Actuator 헬스체크

```bash
# Liveness Probe
curl http://localhost:8080/actuator/health/liveness

# Readiness Probe
curl http://localhost:8080/actuator/health/readiness
```

#### 메트릭 수집

```bash
# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus

# JVM 메트릭
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## FAQ

### Q1. JDK 25 대신 JDK 21로 다운그레이드 가능한가요?

**A:** 네, `build.gradle`에서 `languageVersion = JavaLanguageVersion.of(21)`로 변경하면 됩니다. 대부분의 기능이 호환됩니다.

### Q2. Database(JPA)를 활성화하려면 어떻게 하나요?

**A:** `application.yml`에서 다음 설정 변경:
```yaml
spring:
  autoconfigure:
    exclude: []  # DataSourceAutoConfiguration 제외 해제

feature.db.enabled: true
```

그리고 DataSource 설정 추가:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Q3. Redis로 Rate Limiting 구현하려면?

**A:** `InMemoryRateLimiter` 대신 `RedisRateLimiter` 구현체 작성:
```java
@Component
public class RedisRateLimiter implements RateLimiter {
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Override
    public RateLimitResult isAllowed(String key) {
        // Redis INCR + EXPIRE 명령 사용
    }
}
```

### Q4. 프로덕션에서 Swagger를 특정 IP만 허용하려면?

**A:** `SecurityConfig.java`에서 IP 필터링:
```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
    .access((auth, context) -> {
        String ip = NetworkUtil.getClientIp(context.getRequest());
        return ip.equals("허용할IP") ?
            AuthorizationDecision.grant() :
            AuthorizationDecision.deny();
    })
```

### Q5. 로그를 JSON 포맷으로 출력하려면?

**A:** `logback-spring.xml`에 JSON 인코더 추가:
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
</dependency>
```

```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <includeContext>true</includeContext>
</encoder>
```

---

## 참고 자료

- [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security 가이드](https://docs.spring.io/spring-security/reference/)
- [JJWT 문서](https://github.com/jwtk/jjwt)
- [Resilience4j 문서](https://resilience4j.readme.io/)
- [springdoc-openapi 문서](https://springdoc.org/)
- [Micrometer 문서](https://micrometer.io/docs)

---

## 라이선스

이 프로젝트는 내부 사용 목적으로 작성되었습니다. 필요 시 LICENSE 파일을 추가하세요.

---

## 기여

프로젝트 개선 제안이나 버그 리포트는 이슈를 생성해 주세요.

---

## 연락처

- **작성자**: mingovvv
- **Email**: (추가 필요)
- **프로젝트 경로**: `C:\Users\mk.jang\Desktop\TLC\common-jdk25`

---

**마지막 업데이트**: 2025-01-06
