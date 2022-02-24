package com.umc.btos.src.auth;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.auth.model.*;
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

    // 소셜 로그인
    public AuthGoogleRes logInGoogle(AuthGoogleReq authGoogleReq) throws BaseException {

        // 신규일 경우 회원가입 msg
        if (authProvider.checkEmail(authGoogleReq.getEmail()) == 0) {
            throw new BaseException(AUTH_REQ_SIGNUP); // 회원가입 필요 메시지
        }
        // 탈퇴일 경우
        if (authProvider.checkStatusOfUser(authGoogleReq.getEmail()).equals("deleted")) {
            throw new BaseException(AUTH_REQ_SIGNUP); // 회원가입 필요 메시지
        }
        // 휴면일 경우
        else if (authProvider.checkStatusOfUser(authGoogleReq.getEmail()).equals("dormant")) {
            throw new BaseException(POST_USERS_DORMANT); // 회원 상태 변경 필요 메시지
        }

        // active 유저의 이메일인 경우 로그인 진행
        else if (authProvider.checkEmail(authGoogleReq.getEmail()) == 1) { // 기존 회원이면 jwt 반환
            int userIdx = authDao.idxOfUserWithEmail(authGoogleReq.getEmail()); // userIdx 가져오기
            String jwt = jwtService.createJwt(userIdx);
            return new AuthGoogleRes(userIdx, jwt);
        }
        else { // 로그인 실패
            throw new BaseException(AUTH_FAILED_TO_LOGIN);
        }
    }

    // 디바이스 토큰 갱신
    public void updateToken(PatchTokenReq patchTokenReq, int userIdx) throws BaseException{
        try {
            int result = authDao.updateToken(patchTokenReq.getFcmToken(), userIdx);
            if (result == 0) throw new BaseException(MODIFY_FAIL_FCM_TOKEN);
        }
        //
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
