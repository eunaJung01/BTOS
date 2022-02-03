package com.umc.btos.src.auth;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.auth.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@Repository

public class AuthDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    // 이메일 확인
    public int checkEmail(String email) {
        String checkEmailQuery =
                "select exists(select userIdx from User where email = ? and status IN ('active', 'dormant'))"; // 이메일 중복되는 지 확인
        String checkEmailParam = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParam); //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }

    // 해당 이메일을 가진 유저가 있을 때 상태 확인 -> 탈퇴 후 신규 가입 시 email이 중복되는 값이 되버림
    public String checkStatusOfUser(String email) {
        String checkStatusOfUserQuery = "select status from User where email = ?";
        String checkStatusOfUserParam = email;

        List<String> statusOfUsers =
                this.jdbcTemplate.queryForList(checkStatusOfUserQuery, String.class, checkStatusOfUserParam);

        // 만약 탈퇴회원이 다시 로그인한다 했을 때 active가 존재하면 로그인이고 아니면 회원가입 필요 메시지 날리도록
        for (String status : statusOfUsers) {
            if (status.equals("active") || status.equals("dormant")) {
                return status; // active dormant 검사를 먼저 해야함
            }
        } // 이 반복문을 통과하면 탈퇴회원임

        // 재가입 안 한 탈퇴회원이면 아래 리턴식으로 반환
        return this.jdbcTemplate.queryForObject(checkStatusOfUserQuery, String.class, checkStatusOfUserParam);
    }

    // 해당 userIdx을 가진 유저 상태 체크 -> 휴면이면 재활성화, 탈퇴면 예외 메시지
    public void checkStatusOfUser(int userIdx) throws BaseException {
        String statusOfUserQuery = "select status from User where userIdx = ?";

        if (this.jdbcTemplate.queryForObject(statusOfUserQuery, String.class, userIdx).equals("dormant")) // 휴면 상태면 재활성화
            jdbcTemplate.update("update User set status = 'active', recOthers = 1, recSimilarAge = 1 where userIdx = ?", userIdx);

        else if (this.jdbcTemplate.queryForObject(statusOfUserQuery, String.class, userIdx).equals("deleted")) // 탈퇴 상태면 throw exception
            throw new BaseException(INVALID_JWT);
    }

    // 해당 이메일을 가진 유저의 식별자 반환
    public int idxOfUserWithEmail(String email) {
        String idxOfUserWithEmailQuery = "select userIdx from User where email = ? and status = 'active'";
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
