package com.umc.btos.src.auth;

import com.umc.btos.config.BaseException;
//import com.umc.btos.config.secret.Secret;
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


    // 자동 로그인
    public GetAuthLoginRes authLogIn() throws BaseException {
        try {
            authDao.checkStatusOfUser(jwtService.getUserIdx()); // 휴면 상태 로그인 시 상태 재 활성화, 탈퇴 유저면 오류 메시지
            authDao.updateLastConnect(jwtService.getUserIdx()); // 로그인 기록 갱신
            return new GetAuthLoginRes(jwtService.getUserIdx()); // 유저 idx
        } catch(Exception exception) {
            throw new BaseException(INVALID_JWT);
        }
    }
}
