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
@RequestMapping("/btos")
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
     */

    /**
     *
     */
}
