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
        String checkEmailQuery = "select exists(select email from User where email = ?)"; // 이메일 중복되는 지 확인
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams); //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }

    // 해당 userIdx를 갖는 유저조회
    public GetUserRes getUser(int userIdx) {
        String getUserQuery = "select * from User where userIdx = ?"; // 해당 userIdx를 만족하는 유저를 조회하는 쿼리
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
}
