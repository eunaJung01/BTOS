package com.umc.btos.src.auth;

import com.sun.tracing.dtrace.ProviderAttributes;
import com.umc.btos.config.BaseException;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.auth.model.*;
import com.umc.btos.utils.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class AuthProvider {

    private final AuthDao authDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public AuthProvider(AuthDao authDao, JwtService jwtService) {
        this.authDao = authDao;
        this.jwtService = jwtService;
    }

    // 해당 이메일이 이미 User Table에 존재하는지 확인
    public int checkEmail(String email) throws BaseException {
        try {
            return authDao.checkEmail(email);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 해당 이메일을 가진 유저의 상태 확인
    public String checkStatusOfUser(String email) throws BaseException {
        try {
            return authDao.checkStatusOfUser(email);
        } catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetAuthLoginRes authLogIn() throws BaseException {
        try {
            return new GetAuthLoginRes(jwtService.getUserIdx());
        } catch(Exception exception) {
            throw new BaseException(INVALID_JWT);
        }
    }
}