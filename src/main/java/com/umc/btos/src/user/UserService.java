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

        if (userProvider.checkEmail(postUserReq.getEmail()) == 1) { // 이메일 중복 확인
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
