## Profile & Tech Stack API - 초급 과정

### Phase 1: 기본 설정

- [x]  Spring Boot 프로젝트 생성 (spring-boot-starter-web, spring-boot-starter-jdbc, h2, lombok)
- [x]  application.yml 작성 (H2 설정, SQL 초기화 설정)
- [x]  schema.sql, data.sql 작성
- [x]  애플리케이션 실행 후 H2 Console에서 테이블 확인

### Phase 2: 공통 모듈

- [x]  ApiResponse 클래스 구현
- [x]  Page 제네릭 클래스 구현
- [x]  ErrorCode Enum 정의
- [x]  BusinessException 및 하위 예외 클래스 구현
- [x]  GlobalExceptionHandler 구현

### Phase 3: Profile CRUD

- [x]  Entity 클래스 작성 (Profile, Position Enum)
- [x]  DTO 클래스 작성 (CreateRequest, UpdateRequest, Response)
- [x]  ProfileDao 인터페이스 정의
- [x]  ProfileDaoImpl 구현 (JdbcTemplate + RowMapper)
- [x]  ProfileRepository 구현
- [x]  ProfileService 구현
- [x]  ProfileController 구현
- [x]  cURL 또는 Postman으로 CRUD 테스트

- ### Phase 4: TechStack CRUD

- [x]  Entity 클래스 작성 (TechStack, TechCategory Enum, Proficiency Enum)
- [x]  DTO 클래스 작성 (CreateRequest, UpdateRequest, Response)
- [x]  TechStackDao 인터페이스 정의
- [x]  TechStackDaoImpl 구현 (profileId를 활용한 쿼리 주의)
- [x]  TechStackRepository 구현
- [x]  TechStackService 구현 (프로필 존재 여부 검증 포함)
- [x]  TechStackController 구현
- [x]  cURL 또는 Postman으로 CRUD 테스트

### Phase 5: 페이징 & 검색

- [x]  프로필 목록 페이징 구현 (findAllWithPaging)
- [x]  프로필 검색 구현 (이름 검색, 직무 필터링)
- [x]  기술 스택 목록 페이징 구현 (findByProfileIdWithPaging)
- [x]  기술 스택 검색 구현 (카테고리, 숙련도 필터링)

### Phase 6: 테스트 코드 (보너스)

- [x]  ProfileDao 통합 테스트
- [x]  TechStackDao 통합 테스트
- [x]  페이징 경계 조건 테스트 (첫 페이지, 마지막 페이지, 빈 페이지)

## Profile & Tech Stack API - 고급 과정 리팩토링

### Phase 1: Lombok 적용

- [x]  `build.gradle`에 Lombok 의존성 추가
- [x]  모든 Entity 클래스에 `@Getter`, `@Setter`, `@NoArgsConstructor`, `@Builder` 적용
- [x]  모든 Request DTO에 `@Getter`, `@NoArgsConstructor` 적용
- [x]  모든 Response DTO에 `@Getter`, `@Builder` (또는 `@AllArgsConstructor`) 적용
- [x]  모든 Service, Controller에 `@RequiredArgsConstructor` 적용하여 생성자 주입 코드 제거
- [x]  필요한 곳에 `@Slf4j` 적용하여 로깅 추가
- [x]  수동으로 작성한 Getter/Setter/생성자/Builder 코드 모두 삭제
- [x]  애플리케이션 실행 후 기존 API 동작 확인

### Phase 2: Bean Validation 적용

- [x]  `build.gradle`에 Validation 의존성 추가
- [x]  `ProfileCreateRequest`에 검증 어노테이션 추가 (`@NotBlank`, `@Size`, `@Email`, `@NotNull`, `@Min`)
- [x]  `ProfileUpdateRequest`에 검증 어노테이션 추가 (`@Size`, `@Min`)
- [x]  `TechStackCreateRequest`에 검증 어노테이션 추가
- [x]  `TechStackUpdateRequest`에 검증 어노테이션 추가
- [x]  모든 Controller의 `@RequestBody` 앞에 `@Valid` 추가
- [x]  `GlobalExceptionHandler`에 `MethodArgumentNotValidException` 처리 추가
- [x]  **Service에서 if문 기반 유효성 검증 코드 모두 제거** (비즈니스 로직만 남기기)
- [x]  Postman으로 유효성 검증 실패 케이스 테스트

### Phase 3: MapStruct 적용

- [x]  `build.gradle`에 MapStruct 의존성 추가 (annotationProcessor 순서 주의: Lombok → MapStruct)
- [x]  `ProfileMapper` 인터페이스 작성 (`toEntity`, `toResponse`, `toResponseList`, `updateEntity`)
- [x]  `TechStackMapper` 인터페이스 작성 (`toEntity`, `toResponse`, `toResponseList`, `updateEntity`)
- [x]  `ProfileService`에서 수동 매핑 코드를 `ProfileMapper` 호출로 교체
- [x]  `TechStackService`에서 수동 매핑 코드를 `TechStackMapper` 호출로 교체
- [x]  빌드 후 `build/generated/sources/annotationProcessor`에 Mapper 구현체 생성 확인
- [x]  Postman으로 CRUD API 동작 확인

### Phase 4: Spring Security — 회원 도메인 및 JWT 기반 구현

- [x]  `build.gradle`에 Spring Security, JJWT 의존성 추가
- [x]  `application.yml`에 JWT 설정 추가 (`secret`, `access-token-expiration`, `refresh-token-expiration`)
- [x]  `Member` Entity 작성 (Lombok 적용)
- [x]  `Role` Enum 작성 (`USER`, `ADMIN`)
- [x]  `RefreshToken` Entity 작성
- [x]  `MemberDao` 인터페이스 및 구현체 작성
- [ ]  `RefreshTokenDao` 인터페이스 및 구현체 작성 (`save`, `findByMemberId`, `deleteByMemberId`)
- [ ]  `CustomUserDetailsService` 구현 (`UserDetailsService` 구현체)
- [ ]  `PasswordEncoder` Bean 등록 (`BCryptPasswordEncoder`)
- [ ]  `SignupRequest` 작성 (Bean Validation 적용)
- [ ]  `LoginRequest`, `TokenRefreshRequest` 작성
- [ ]  `LoginResponse` (accessToken + refreshToken), `TokenRefreshResponse` (accessToken) 작성

### Phase 5: Spring Security — JWT 필터 및 설정

- [ ]  `JwtTokenProvider` 구현 (`createAccessToken`, `createRefreshToken`, `getUsername`, `validateToken`)
- [ ]  `JwtAuthenticationFilter` 구현 (`OncePerRequestFilter` 상속, `shouldNotFilter()` 오버라이드)
- [ ]  `JwtAuthenticationEntryPoint` 구현 (토큰 에러 분기: `TOKEN_EXPIRED`, `INVALID_TOKEN`, `UNAUTHORIZED`)
- [ ]  `SecurityConfig` 작성
    - [ ]  CSRF 비활성화
    - [ ]  세션 정책 `STATELESS` 설정
    - [ ]  인가 규칙 설정 (GET은 허용, CUD는 인증 필요)
    - [ ]  `addFilterBefore`로 `JwtAuthenticationFilter` 등록
    - [ ]  `JwtAuthenticationEntryPoint` 등록
- [ ]  `AuthService` 구현 (회원가입, 로그인, 토큰 재발급, 로그아웃)
- [ ]  `AuthController` 구현 (signup, login, refresh, logout 엔드포인트)
- [ ]  Postman으로 회원가입 → 로그인 → Access Token으로 API 호출 테스트

### Phase 6: 소유권 검증 적용

- [ ]  `profile` 테이블에 `member_id` 컬럼 추가 (schema.sql 수정)
- [ ]  `Profile` Entity에 `memberId` 필드 추가
- [ ]  `ProfileDao`에 `member_id` 관련 쿼리 추가 (`findByMemberId`, INSERT 시 `member_id` 포함)
- [ ]  `ProfileService`에 소유권 검증 로직 추가 (수정/삭제 시 `memberId` 확인)
- [ ]  `TechStackService`에 소유권 검증 로직 추가 (해당 프로필의 소유자인지 확인)
- [ ]  `ProfileController`에 `@AuthenticationPrincipal UserDetails` 파라미터 추가
- [ ]  `TechStackController`에 `@AuthenticationPrincipal UserDetails` 파라미터 추가
- [ ]  `UnauthorizedException` 작성 + `GlobalExceptionHandler`에 처리 추가
- [ ]  Postman으로 소유권 검증 테스트 (A 사용자로 B의 프로필 수정 시도 → 거부 확인)