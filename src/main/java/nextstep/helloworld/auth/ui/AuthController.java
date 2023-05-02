package nextstep.helloworld.auth.ui;

import nextstep.helloworld.auth.application.AuthService;
import nextstep.helloworld.auth.application.AuthorizationException;
import nextstep.helloworld.auth.dto.AuthInfo;
import nextstep.helloworld.auth.dto.MemberResponse;
import nextstep.helloworld.auth.infrastructure.AuthorizationExtractor;
import nextstep.helloworld.auth.dto.TokenRequest;
import nextstep.helloworld.auth.dto.TokenResponse;
import nextstep.helloworld.auth.infrastructure.BasicAuthorizationExtractor;
import nextstep.helloworld.auth.infrastructure.BearerAuthorizationExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
public class AuthController {
    private static final String SESSION_KEY = "USER";
    private static final String USERNAME_FIELD = "email";
    private static final String PASSWORD_FIELD = "password";
    private AuthorizationExtractor<String> bearerAuthorizationExtractor = new BearerAuthorizationExtractor();
    private AuthorizationExtractor<AuthInfo> basicAuthorizationExtractor = new BasicAuthorizationExtractor();
    private AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/session")
    public ResponseEntity sessionLogin(final HttpServletRequest request,final HttpSession session) {
        final Map<String, String[]> paramMap = request.getParameterMap();
        final String email = paramMap.get(USERNAME_FIELD)[0];
        final String password = paramMap.get(PASSWORD_FIELD)[0];

        if (authService.checkInvalidLogin(email, password)) {
            throw new AuthorizationException();
        }

        session.setAttribute(SESSION_KEY, email);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/members/me")
    public ResponseEntity findMyInfo(HttpSession session) {
        String email = (String) session.getAttribute(SESSION_KEY);
        MemberResponse member = authService.findMember(email);
        return ResponseEntity.ok().body(member);
    }

    @PostMapping("/login/token")
    public ResponseEntity tokenLogin(@RequestBody TokenRequest tokenRequest) {
        TokenResponse tokenResponse = authService.createToken(tokenRequest);
        return ResponseEntity.ok().body(tokenResponse);
    }

    @GetMapping("/members/you")
    public ResponseEntity findYourInfo(HttpServletRequest request) {
        String token = bearerAuthorizationExtractor.extract(request);
        MemberResponse member = authService.findMemberByToken(token);
        return ResponseEntity.ok().body(member);
    }

    /**
     * ex) request sample
     * <p>
     * GET /members/my HTTP/1.1
     * authorization: Basic ZW1haWxAZW1haWwuY29tOjEyMzQ=
     * accept: application/json
     */
    @GetMapping("/members/my")
    public ResponseEntity findMyInfo(HttpServletRequest request) {
        AuthInfo authInfo = basicAuthorizationExtractor.extract(request);
        String email = authInfo.getEmail();
        String password = authInfo.getPassword();

        if (authService.checkInvalidLogin(email, password)) {
            throw new AuthorizationException();
        }

        MemberResponse member = authService.findMember(email);
        return ResponseEntity.ok().body(member);
    }
}
