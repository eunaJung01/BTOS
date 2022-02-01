package com.umc.btos.src.user;

import com.umc.btos.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository

public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 회원가입
    public int createUser(PostUserReq postUserReq) {
        String createUserQuery = "insert into User (email, birth, nickName) VALUES (?,?,?)"; // email, birth, nickName 삽입 동적 쿼리
        Object[] createUserParams = new Object[]{
                postUserReq.getEmail(),
                postUserReq.getBirth(),
                postUserReq.getNickName()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInsertIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class);
    }

    // 이메일 확인
    public int checkEmail(String email) {
        String checkEmailQuery = "select exists(select email from User where email = ? and status in ('active', 'dormant'))"; // 이메일 중복되는 지 확인(탈퇴 후 재가입 고려하여 active인 유저 중에서만 고려)
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams); //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }

    // 회원 상태 변경
    public int changeStatusOfUser(PatchUserReq patchUserReq) {
        String changeStatusQuery = "";
        Object[] changeStatusParams = new Object[]{patchUserReq.getStatus() ,patchUserReq.getUserIdx()};

        System.out.println("상태값 : "+patchUserReq.getStatus());
        if (patchUserReq.getStatus().equals("active")) // 재 활성화의 경우
            changeStatusQuery = "update User set status = ?, recOthers = 1, recSimilarAge = 1 where userIdx = ?";

        else if (patchUserReq.getStatus().equals("dormant") || patchUserReq.getStatus().equals("deleted")) // 휴면 or 탈퇴의 경우
            changeStatusQuery = "update User set status = ?, recOthers = 0, recSimilarAge = 0 where userIdx = ?";

        return this.jdbcTemplate.update(changeStatusQuery, changeStatusParams);
        // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

    // 해당 userIdx를 갖는 유저조회
    public GetUserRes getUser(int userIdx) {
        String getUserQuery = "select * from User where userIdx = ? and status = 'active'"; // 해당 userIdx를 만족하는 유저를 조회하는 쿼리
        int getUserParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUserQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("email"),
                        rs.getString("nickName"),
                        rs.getInt("birth"),
                        rs.getInt("selectedPlantIdx"),
                        rs.getString("isPremium"),
                        rs.getBoolean("recOthers"),
                        rs.getBoolean("recSimilarAge"),
                        rs.getInt("fontIdx"),
                        rs.getBoolean("pushAlarm")),
                        getUserParams);
    }

    // 닉네임 확인
    public int checkNickName(String nickName) {
        String checkNickNameQuery = "select exists(select nickName from User where nickName = ? and status in ('active', 'dormant'))";
        String checkNickNameParams = nickName;
        return this.jdbcTemplate.queryForObject(checkNickNameQuery, int.class, checkNickNameParams);
        //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }

    // 닉네임 변경
    public int modifyUserNickName(PatchUserNickNameReq patchUserNickNameReq){
        String modifyUserNickNameQuery = "update User set nickName = ? where userIdx = ?";
        Object[] modifyUserNickNameParams = new Object[]{patchUserNickNameReq.getNickName(), patchUserNickNameReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyUserNickNameQuery, modifyUserNickNameParams);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

    // 생년 변경
    public int modifyUserBirth(PatchUserBirthReq patchUserBirthReq) {
        String modifyUserBirthQuery = "update User set birth = ? where userIdx = ?";
        Object[] modifyUserBirthParams = new Object[]{patchUserBirthReq.getBirth(), patchUserBirthReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyUserBirthQuery, modifyUserBirthParams);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

    // 다른 사람 수신 설정
    public int modifyReceiveOthers(PatchUserRecOthersReq patchUserReceiveOthersReq) {
        String modifyReceiveOthersQuery = "update User set recOthers = ? where userIdx = ?";
        Object[] modifyReceiveOthersParams = new Object[]{patchUserReceiveOthersReq.isRecOthers(), patchUserReceiveOthersReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyReceiveOthersQuery, modifyReceiveOthersParams);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

    // 비슷한 연령대 수신 설정
    public int modifyReceiveSimilarAge(PatchUserRecSimilarAgeReq patchUserRecSimilarAgeReq) {
        String modifyReceiveOthersQuery = "update User set recSimilarAge = ? where userIdx = ?";
        Object[] modifyReceiveOthersParams = new Object[]{patchUserRecSimilarAgeReq.isRecSimilarAge(), patchUserRecSimilarAgeReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyReceiveOthersQuery, modifyReceiveOthersParams);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

    // 푸시 알림 수신 변경
    public int modifyPushAlarm(PatchUserPushAlarmReq patchUserPushAlarmReq) {
        String modifyPushAlarmQuery = "update User set pushAlarm = ? where userIdx = ?";
        Object[] modifyPushAlarmParams = new Object[] {patchUserPushAlarmReq.isPushAlarm(), patchUserPushAlarmReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyPushAlarmQuery, modifyPushAlarmParams);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

    // 폰트 변경
    public int changeFont(PatchUserFontReq patchUserFontReq) {
        String changeFontQuery = "update User set fontIdx = ? where userIdx = ?";
        Object[] changeFontParams = new Object[] {patchUserFontReq.getFontIdx(), patchUserFontReq.getUserIdx()};
        return this.jdbcTemplate.update(changeFontQuery, changeFontParams);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

    // 시무룩이 상태 변경
    public int changeIsSad(PatchUserIsSadReq patchUserIsSadReq) {
        String changeIsSadQuery = "update User set isSad = ? where userIdx = ?";
        Object[] changeIsSadParams = new Object[] {patchUserIsSadReq.isIsSad(), patchUserIsSadReq.getUserIdx()};
        return this.jdbcTemplate.update(changeIsSadQuery, changeIsSadParams);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

    // 정해진 시간마다 마지막 로그인 기록과 현재 시간과의 차이 계산
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 미접속 기간 체크 하도록 주기 설정
    public void checkLastConnect() {
        // 마지막 로그인 시간과 현재 시간이 5일 이상 차이 나는 userIdx 추출
        String checkLastConnectQuery = "select userIdx from User where TIMESTAMPDIFF(day, lastConnect, CURRENT_TIMESTAMP) >= 5";
        // 휴면으로 바꿔야할 userIdx 배열에 저장
        ArrayList<Integer> needToDormantUsers = new ArrayList<>(this.jdbcTemplate.queryForList(checkLastConnectQuery, int.class)); // 쿼리 결과를 배열에 저장하도록함

        // 휴면 처리 -> 수신 차단까지
        String changeStatusQuery = "update User set status = 'dormant', recOthers = 0, recSimilarAge = 0 where userIdx = ?";
        for (int userIdx : needToDormantUsers)
            this.jdbcTemplate.update(changeStatusQuery, userIdx);

    }

}
