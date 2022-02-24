package com.umc.btos.src.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.auth.model.*;
import com.umc.btos.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.umc.btos.config.BaseResponseStatus.*;
import static com.umc.btos.utils.ValidationRegex.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final AuthProvider authProvider;
    @Autowired
    private final AuthService authService;
    @Autowired
    private final JwtService jwtService;

    public AuthController(AuthProvider authProvider,
                          AuthService authService,
                          JwtService jwtService){
        this.authProvider = authProvider;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * 소셜(구글) 로그인 API : 클라에서 받은 정보 body로 받고 회원 상태에 따라 메시지, jwt 반환, fcm 토큰 저장
     * [POST] /auth/google
     */

    @ResponseBody
    @PostMapping("/google")
    public BaseResponse<AuthGoogleRes> googleLogin(@RequestBody AuthGoogleReq authGoogleReq) {

        try{
            // // ***형식적 validation***
            // email 값 존재 검사
            if (authGoogleReq.getEmail() == null) {
                return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
            }
            // email 형식 검사
            if (!isRegexEmail(authGoogleReq.getEmail())) {
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            }

            AuthGoogleRes authGoogleRes = authService.logInGoogle(authGoogleReq);
            return new BaseResponse<>(authGoogleRes);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 자동로그인 API : header에 jwt 넣고 api 호출 - 유저의 idx 요청 - 담긴 정보를 response
     * [GET] /auth/jwt
     */
    @ResponseBody
    @GetMapping("/jwt")
    public BaseResponse<GetAuthLoginRes> authLogIn() {
        try {
            GetAuthLoginRes getAuthLoginRes = authProvider.authLogIn();
            return new BaseResponse<>(getAuthLoginRes);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 디바이스 토큰 갱신 API
     * [PATCH] /auth/token?userIdx=
     */
    @ResponseBody
    @PatchMapping("/token")
    public BaseResponse<String> updateToken(@RequestParam int userIdx, @RequestBody PatchTokenReq patchTokenReq) {
        try {
            PatchTokenReq tokenReq = new PatchTokenReq(patchTokenReq.getFcmToken());
            authService.updateToken(tokenReq, userIdx);
            String result = "디바이스 토큰이 갱신되었습니다.";

            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
