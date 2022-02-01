package com.umc.btos.src.auth;

import com.umc.btos.src.auth.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sound.midi.Patch;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository

public class AuthDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    // 이메일 확인
    public int checkEmail(String email) {
        String checkEmailQuery = "select exists(select userIdx from User where email = ?)"; // 이메일 중복되는 지 확인
        String checkEmailParam = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParam); //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }

    // 해당 이메일을 가진 유저가 있을 때 상태 확인
    public String checkStatusOfUser(String email){
        String checkStatusOfUserQuery = "select status from User where email = ?";
        String checkStatusOfUserParam = email;
        return this.jdbcTemplate.queryForObject(checkStatusOfUserQuery, String.class, checkStatusOfUserParam);
    }

    // 해당 userIdx을 가진 유저가 휴면 상태면 재활성화
    public void checkStatusOfUser(int userIdx){
        String statusOfUserQuery = "select status from User where userIdx = ?";

        if (this.jdbcTemplate.queryForObject(statusOfUserQuery, String.class, userIdx).equals("dormant")) // 휴면 상태면 재활성화
            jdbcTemplate.update("update User set status = 'active', recOthers = 1, recSimilarAge = 1 where userIdx = ?", userIdx);

    }

    // 해당 이메일을 가진 유저의 식별자 반환
    public int idxOfUserWithEmail(String email) {
        String idxOfUserWithEmailQuery = "select userIdx from User where email = ?";
        String idxOfUserWithEmailParam = email;
        return this.jdbcTemplate.queryForObject(idxOfUserWithEmailQuery, int.class, idxOfUserWithEmailParam);
    }
    
    // 로그인 기록 갱신
    public int updateLastConnect(int userIdx) {
        String lastConnectQuery = "update User set lastConnect = CURRENT_TIMESTAMP where userIdx = ?";
        int lastConnectParam = userIdx;
        return this.jdbcTemplate.update(lastConnectQuery, lastConnectParam);
        // 대응시켜 매핑시켜 쿼리 요청(변경했으면 1, 실패했으면 0)
    }

}
