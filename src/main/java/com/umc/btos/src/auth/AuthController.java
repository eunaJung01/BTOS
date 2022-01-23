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
@RequestMapping("/btos/auth")
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
     * 소셜(구글) 로그인 API : 클라에서 받은 정보 body로 받고 회원 상태에 따라 메시지, jwt 반환
     * [POST] /btos/auth/google
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
    
}
