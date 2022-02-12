package com.umc.btos.src.googleOAuth;

import com.umc.btos.src.googleOAuth.model.GoogleOAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@RequiredArgsConstructor

public class OAuthService {
    private final GoogleOAuth googleOAuth;
    private final HttpServletResponse response;

    // googleLogin 로그인 처리 : 리디렉션 url 생성, 리디렉션
    public void request() {
        String redirectURL = googleOAuth.getOauthRedirectURL();

        try {
            response.sendRedirect(redirectURL); //토큰 요청을 하도록 리다이렉트
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // callback 메소드 처리 : access token 등 발급
    public String requestAccessToken(String code) {
        return googleOAuth.requestAccessToken(code);
    }
}
