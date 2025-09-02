package com.penglobe.server;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.AuthDTO.*;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.security.JwtTokenProvider;
import com.penglobe.server.service.AuthService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("AuthService 단위테스트")
class AuthServiceSignupLocalUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    //@Test
    @DisplayName("정상 회원가입: 이메일 중복 아님 → save 호출되고 필드값이 올바르다(콘솔 출력 포함)")
    void signupLocal_success_prints() {
        // given
        LocalSignupRequest req = new LocalSignupRequest();
        req.email = "test@penglobe.com";
        req.password = "Password123!";
        req.nickname = "테스터";
        req.regionId = 101;
        req.profileId = 9;

        when(userRepository.existsByEmail(req.email)).thenReturn(false);
        when(passwordEncoder.encode(req.password)).thenReturn("ENCODED_PW");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // when
        authService.signupLocal(req);

        // then
        User saved = captor.getValue();

        // 콘솔 출력 (포인트 테스트처럼 눈으로 확인용)
        System.out.println("==== 회원가입 저장 값 ====");
        System.out.println("email: " + saved.getEmail());
        System.out.println("passwordHash: " + saved.getPasswordHash());
        System.out.println("nickname: " + saved.getNickname());
        System.out.println("regionId: " + saved.getRegionId());
        System.out.println("profileId: " + saved.getProfileId());
        System.out.println("isProfileComplete: " + saved.getIsProfileComplete());

        // 값 검증
        assertThat(saved.getEmail()).isEqualTo("test@penglobe.com");
        assertThat(saved.getPasswordHash()).isEqualTo("ENCODED_PW");
        assertThat(saved.getNickname()).isEqualTo("테스터");
        assertThat(saved.getRegionId()).isEqualTo(101);
        assertThat(saved.getProfileId()).isEqualTo(9);
        assertThat(saved.getIsProfileComplete()).isTrue();

        // 저장 호출 검증
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("정상 로그인: 이메일 존재 + 비밀번호 일치 → JWT 발급 (콘솔 출력)")
    void loginLocal_success_prints() {
        // given
        LocalLoginRequest req = new LocalLoginRequest();
        req.email = "login@penglobe.com";
        req.password = "pw1234";

        User user = User.builder()
                .userId(3L)
                .email("login@penglobe.com")
                .passwordHash("ENCODED_PW")
                .isProfileComplete(true)
                .build();

        when(userRepository.findByEmail(req.email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.password, "ENCODED_PW")).thenReturn(true);
        when(jwtTokenProvider.createToken(3L, "USER")).thenReturn("jwt.token");

        // when
        AuthResponse res = authService.loginLocal(req);

        // then
        System.out.println("==== 로그인 성공 ====");
        System.out.println("JWT: " + res.token);
        System.out.println("userId: " + res.userId);
        System.out.println("profileCompleted: " + res.profileCompleted);

        assertThat(res.token).isEqualTo("jwt.token");
        assertThat(res.userId).isEqualTo(3L);
        assertThat(res.profileCompleted).isTrue();
    }

}
