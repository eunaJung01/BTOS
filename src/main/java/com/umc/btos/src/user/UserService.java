package com.umc.btos.src.user;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.user.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;


@Service
public class UserService {

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분: Log를 기록하기 위해 필요한 함수입니다.

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;

    @Autowired
    public UserService(UserDao userDao,
                       UserProvider userProvider,
                       JwtService jwtService) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;

    }

    // 회원가입(POST)
    public PostUserRes createUser(PostUserReq postUserReq) throws BaseException{

        // 회원 상태 확인 : 소셜 로그인 버튼 클릭 회원 정보 가져왔음 (소셜 로그인 api)
        // 만약 status가 dormant라면 휴면 상태 계정입니다. 재활성화 하시겠습니까? 버튼 누르면 회원 상태 변경 api(재활성화) 호출하도록 만들기
        // 휴면 아니면 신규 or 탈퇴 회원 -> 회원가입 api
        // deleted인 경우, 신규인 경우 : 다시 회원가입 진행(새로운 레코드 생성) -> 회원가입 api 호출


        if (userProvider.checkStatusOfUser(postUserReq.getEmail()) == "dormant") { // 휴면일 경우 메시지 출력
            throw new BaseException(POST_USERS_IS_DORMANT);
        }
        
        // status : deleted or 신규회원일 경우 중복 체크
        if (userProvider.checkEmail(postUserReq.getEmail()) == 1) { // 이메일 중복 확인 -> active인 유저들만 조회
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        try{
            int userIdx = userDao.createUser(postUserReq);
            return new PostUserRes(userIdx);

        } catch (Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
