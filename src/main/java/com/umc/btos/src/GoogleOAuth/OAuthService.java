package com.umc.btos.src.GoogleOAuth;

import com.umc.btos.src.GoogleOAuth.model.GoogleOAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@RequiredArgsConstructor

public class OAuthService {
    private final GoogleOAuth googleOAuth;
    private final HttpServletResponse response;

    /**
     * 구글 로그인 처리
     */
    public void request() {
        String redirectURL = googleOAuth.getOauthRedirectURL();

        try {
            response.sendRedirect(redirectURL); //토큰 요청을 하도록 리다이렉트
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 로그인 처리 후 토큰 요청
     * @param code
     * @return
     */
    public String requestAccessToken(String code) {
        return googleOAuth.requestAccessToken(code);
    }
}
