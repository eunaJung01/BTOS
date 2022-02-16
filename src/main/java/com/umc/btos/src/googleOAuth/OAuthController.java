package com.umc.btos.src.googleOAuth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/google-login")
@RequiredArgsConstructor

public class OAuthController {
    private final OAuthService oAuthService;

    /**
     * 구글 로그인 요청을 받아서 처리
     * [GET] /auth/google-login
     */
    @GetMapping("")
    public void googleLogin() {
        oAuthService.request();
        return;
    }

    /**
     * access token 발급
     * [GET] /auth/google-login/callback
     *
     * 위 googleLogin 메소드 호출하면 callback 메소드 리디렉션 됨
     *
     * @param code
     * @return 로그인 요청 결과로 받은 Json 형태의 String 문자열 (access_token, refresh_token 등)
     */
    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code) {
        return oAuthService.requestAccessToken(code);
    }
}
