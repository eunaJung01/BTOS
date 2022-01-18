package com.umc.btos.src.auth;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.auth.model.*;
import com.umc.btos.utils.AES128;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;


@Service
public class AuthService {

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분: Log를 기록하기 위해 필요한 함수입니다.

    private final AuthDao authDao;
    private final AuthProvider authProvider;
    private final JwtService jwtService;

    @Autowired
    public AuthService(AuthDao authDao,
                       AuthProvider authProvider,
                       JwtService jwtService) {
        this.authDao = authDao;
        this.authProvider = authProvider;
        this.jwtService = jwtService;

    }

    // 회원가입(POST)
    public PostAuthRes createUser(PostAuthReq postAuthReq) throws BaseException{

        if (authProvider.checkEmail(postAuthReq.getEmail()) == 1) { // 이메일 중복 확인
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        try{
            int userIdx = authDao.createUser(postAuthReq);
            return new PostAuthRes(userIdx);

        } catch (Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
