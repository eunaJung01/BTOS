package com.umc.btos.src.user;

import com.umc.btos.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

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
        String checkEmailQuery = "select exists(select email from User where email = ? and status IS NOT 'deleted')"; // 이메일 중복되는 지 확인(탈퇴 후 재가입 고려하여 active인 유저 중에서만 고려)
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams); //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }

    // 회원 상태 변경
    public int changeStatusOfUser(PatchUserReq patchUserReq) {
        String changeStatusQuery = "update User set status = ?, updatedAt = CURRENT_TIMESTAMP where userIdx = ?";
        Object[] changeStatusParams = new Object[]{patchUserReq.getStatus() ,patchUserReq.getUserIdx()};
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
        String checkNickNameQuery = "select exists(select nickName from User where nickName = ? and status IS NOT 'deleted')";
        String checkNickNameParams = nickName;
        return this.jdbcTemplate.queryForObject(checkNickNameQuery, int.class, checkNickNameParams);
        //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }

    // 닉네임 변경
    public int modifyUserNickName(PatchUserInfoReq patchUserInfoReq){
        String modifyUserNickNameQuery = "update User set nickName = ?, updatedAt=CURRENT_TIMESTAMP where userIdx = ?";
        Object[] modifyUserNickNameParams = new Object[]{patchUserInfoReq.getNickName(), patchUserInfoReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyUserNickNameQuery, modifyUserNickNameParams);
        // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

    // 생년 변경
    public int modifyUserBirth(PatchUserInfoReq patchUserInfoReq) {
        String modifyUserBirthQuery = "update User set birth = ?, updatedAt=CURRENT_TIMESTAMP where userIdx = ?";
        Object[] modifyUserBirthParams = new Object[]{patchUserInfoReq.getBirth(), patchUserInfoReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyUserBirthQuery, modifyUserBirthParams);
        // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

    // 닉네임, 생년 변경
    public int modifyUserInfo(PatchUserInfoReq patchUserInfoReq) {
        String modifyUserInfoQuery = "update User set nickName = ?, birth = ?, updatedAt=CURRENT_TIMESTAMP where userIdx = ?";
        Object[] modifyUserInfoParams = new Object[]{patchUserInfoReq.getNickName(), patchUserInfoReq.getBirth(), patchUserInfoReq.getUserIdx()};
        return this.jdbcTemplate.update(modifyUserInfoQuery, modifyUserInfoParams);
        // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

}
