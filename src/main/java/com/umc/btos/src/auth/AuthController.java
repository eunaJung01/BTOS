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
     * 회원가입 API
     * [POST] /btos/auth/sign-up
     */

    @PostMapping("/sign-up")
    public BaseResponse<PostAuthRes> createUser(@RequestBody PostAuthReq postAuthReq){
        // 형식적 validation은 클라이언트 단에서 처리
        try {
            PostAuthRes postAuthRes = authService.createUser(postAuthReq);
            return new BaseResponse<>(postAuthRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }



    /**
     * 소셜 로그인 API
     * [GET] /btos/auth/google
     */

}
