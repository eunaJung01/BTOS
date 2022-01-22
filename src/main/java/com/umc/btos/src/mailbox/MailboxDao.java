package com.umc.btos.src.mailbox;

import com.umc.btos.src.mailbox.model.*;
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

    // 편지 조회
    public GetLetterRes getLetter(int letterIdx) {
        String query = "SELECT * FROM Letter WHERE letterIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetLetterRes(
                        rs.getInt("letterIdx"),
                        rs.getInt("replierIdx"),
                        rs.getInt("receiverIdx"),
                        rs.getString("content")
                ), letterIdx);
    }

    // 답장 조회
    public GetReplyRes getReply(int replyIdx) {
        String query = "SELECT * FROM Reply WHERE replyIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetReplyRes(
                        rs.getInt("replyIdx"),
                        rs.getInt("replierIdx"),
                        rs.getInt("receiverIdx"),
                        rs.getString("content")
                ), replyIdx);
    }

    // User.fontIdx 반환
    public int getFontIdx(String type, int idx) {
        // type : Table 명 (Diary / Letter / Reply)
        String typeIdx = type + "Idx"; // 해당 Table의 식별자 명 (Diary : diaryIdx / Letter : letterIdx / Reply : replyIdx)
        String columnName_sender = "replierIdx"; // 발신자 Column 명 (Diary : userIdx / Letter & Reply : replierIdx)

        if (type.compareTo("diary") == 0) { // 일기
            type = "Diary";
            columnName_sender = "userIdx";
        } else if (type.compareTo("letter") == 0) { // 편지
            type = "Letter";
        } else { // 답장
            type = "Reply";
        }

        String query = "SELECT fontIdx FROM User WHERE userIdx = (SELECT " + columnName_sender + " FROM " + type + " WHERE " + typeIdx + " = ? AND status = 'active') AND status = 'active'";
        // ex. SELECT fontIdx FROM User WHERE userIdx = (SELECT replierIdx FROM Reply WHERE replierIdx = ? AND status = 'active') AND status = 'active'
        return this.jdbcTemplate.queryForObject(query, int.class, idx);
    }

}
