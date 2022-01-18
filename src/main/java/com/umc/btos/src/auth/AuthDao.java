package com.umc.btos.src.auth;

import com.umc.btos.src.auth.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sound.midi.Patch;
import javax.sql.DataSource;
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
        String checkEmailQuery = "select exists(select email from User where email = ?)"; // 이메일 중복되는 지 확인
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams); //결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환
    }
}
