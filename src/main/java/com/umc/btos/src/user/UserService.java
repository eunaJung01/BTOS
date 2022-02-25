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
    private final UserFcmService fcmService;

    @Autowired
    public UserService(UserDao userDao,
                       UserProvider userProvider,
                       JwtService jwtService,
                       UserFcmService fcmService) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
        this.fcmService = fcmService;

    }

    // 회원가입(POST)
    public PostUserRes createUser(PostUserReq postUserReq) throws BaseException{

        // 회원 상태 확인 : 소셜 로그인 버튼 클릭 회원 정보 가져왔음 (소셜 로그인 api)
        // 만약 status가 dormant라면 휴면 상태 계정입니다. 재활성화 하시겠습니까? 버튼 누르면 회원 상태 변경 api(재활성화) 호출하도록 만들기
        // 휴면 아니면 신규 or 탈퇴 회원 -> 회원가입 api
        // deleted인 경우, 신규인 경우 : 다시 회원가입 진행(새로운 레코드 생성) -> 회원가입 api 호출

        // status : deleted or 신규회원일 경우 중복 체크
        if (userProvider.checkEmail(postUserReq.getEmail()) == 1) { // 이메일 중복 확인 -> deleted 아닌 유저들한테서만 조회
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        // 닉네임 중복 체크
        if (userProvider.checkNickName(postUserReq.getNickName()) == 1) {
            throw new BaseException(PATCH_USERS_EXISTS_NICKNAME);
        }

        // 회원 가입
        try{
            int userIdx = userDao.createUser(postUserReq);

            // 푸시 알림 전송
            // 해당 유저의 fcmToken을 가져옴
            String token = userDao.getToken(userIdx);
            String title = "환영합니다" + postUserReq.getNickName();
            String body = "저편너머로부터 편지가 도착했어요.";
            fcmService.sendMessageTo(token, title, body);

            return new PostUserRes(userIdx);
        } catch (Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원 상태 변경(PATCH)
    public void changeStatusOfUser(PatchUserReq patchUserReq) throws BaseException {
        try {
            int result = userDao.changeStatusOfUser(patchUserReq);
            if (result == 0) throw new BaseException(MODIFY_FAIL_STATUS); // 상태 변경 실패시 에러 메시지

        }
        catch (Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 닉네임 변경(PATCH)
    public void modifyUserNickName(PatchUserNickNameReq patchUserNickNameReq) throws BaseException {
        // 닉네임 중복 확인 -> deleted 아닌 유저들한테서만 조회
        if (userProvider.checkNickName(patchUserNickNameReq.getNickName()) == 1) { // 이미 있으면 1
            throw new BaseException(PATCH_USERS_EXISTS_NICKNAME);
        }
        try {
            int result = userDao.modifyUserNickName(patchUserNickNameReq); // result = 0 이 나오고 있음
            if (result == 0) throw new BaseException(MODIFY_FAIL_INFO); // 닉네임 변경 실패시 에러 메시지

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 생년 변경(PATCH)
    public void modifyUserBirth(PatchUserBirthReq patchUserBirthReq) throws BaseException {
        try {
            int result = userDao.modifyUserBirth(patchUserBirthReq);
            if (result == 0) throw new BaseException(MODIFY_FAIL_INFO); // 생년 변경 실패시 에러 메시지

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 다른 사람 수신 설정(PATCH)
    public void modifyReceiveOthers(PatchUserRecOthersReq patchUserReceiveOthersReq) throws BaseException {
        try {
            //userDao.checkLastConnect();
            int result = userDao.modifyReceiveOthers(patchUserReceiveOthersReq);
            if (result == 0) throw new BaseException(MODIFY_FAIL_RECEIVE_OTHERS); // 푸시 알람 수신 여부 변경 실패시 에러 메시지

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 비슷한 연령대 수신 설정(PATCH)
    public void modifyReceiveSimilarAge(PatchUserRecSimilarAgeReq patchUserRecSimilarAgeReq) throws BaseException {
        try {
            int result = userDao.modifyReceiveSimilarAge(patchUserRecSimilarAgeReq);
            if (result == 0) throw new BaseException(MODIFY_FAIL_RECEIVE_OTHERS); // 푸시 알람 수신 여부 변경 실패시 에러 메시지

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 푸시 알림 수신 여부 변경(PATCH)
    public void modifyPushAlarm(PatchUserPushAlarmReq patchUserPushAlarmReq) throws BaseException{
        try {
            int result = userDao.modifyPushAlarm(patchUserPushAlarmReq);
            if (result == 0) throw new BaseException(MODIFY_FAIL_PUSH_ALARM); // 푸시 알람 수신 여부 변경 실패시 에러 메시지

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 폰트 변경(PATCH)
    public void changeFont(PatchUserFontReq patchUserFontReq) throws BaseException{
        try {
            int result = userDao.changeFont(patchUserFontReq);
            if (result == 0) throw new BaseException(CHANGE_FAIL_FONT); // 폰트 변경 실패시 에러 메시지

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 시무룩이 상태 변경(PATCH)
    public void changeIsSad(PatchUserIsSadReq patchUserIsSadReq) throws BaseException{
        try {
            int result = userDao.changeIsSad(patchUserIsSadReq);
            if (result == 0) throw new BaseException(CHANGE_FAIL_IS_SAD); // 시무룩이 상태 변경 실패시 에러 메시지

        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
