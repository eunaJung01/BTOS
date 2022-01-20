package com.umc.btos.src.mailbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class MailboxDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // User.fontIdx 반환
    public int getFontIdx(int diaryIdx) {
        String query = "SELECT fontIdx FROM User WHERE userIdx = (SELECT userIdx FROM Diary WHERE diaryIdx = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

}
