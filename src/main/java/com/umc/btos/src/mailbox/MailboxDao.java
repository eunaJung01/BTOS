package com.umc.btos.src.mailbox;

import com.umc.btos.src.mailbox.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class MailboxDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // =================================== 우편함 조회 ===================================

    // 우편함 조회 - 일기 수신 목록
    public List<GetMailboxRes> getMailbox_diary(int userIdx) {
        String query = "SELECT Diary.diaryIdx AS idx, User.nickName AS senderNickName, Diary.updatedAt AS sendAt " +
                "FROM Diary " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN DiarySendList ON Diary.diaryIdx = DiarySendList.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND DiarySendList.isChecked = 0";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetMailboxRes(
                        "diary",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt"),
                        true
                ), userIdx);
    }

    // 우편함 조회 - 편지 수신 목록
    public List<GetMailboxRes> getMailbox_letter(int userIdx) {
        String query = "SELECT Letter.letterIdx AS idx, User.nickName AS senderNickName, Letter.updatedAt AS sendAt " +
                "FROM Letter " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "INNER JOIN LetterSendList ON Letter.letterIdx = LetterSendList.letterIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND LetterSendList.isChecked = 0";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetMailboxRes(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt"),
                        false
                ), userIdx);
    }

    // 우편함 조회 - 답장 수신 목록
    public List<GetMailboxRes> getMailbox_reply(int userIdx) {
        String query = "SELECT Reply.replyIdx AS idx, User.nickName AS senderNickName, Reply.createdAt AS sendAt " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND Reply.isChecked = 0";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetMailboxRes(
                        "reply",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt"),
                        false
                ), userIdx);
    }

    // =================================== 우편 조회 ===================================

    // 편지 조회
    public GetLetterRes getLetter(int letterIdx) {
        String query = "SELECT Letter.letterIdx, Letter.userIdx AS replierIdx, LetterSendList.receiverIdx, Letter.content " +
                "FROM Letter " +
                "INNER JOIN LetterSendList ON Letter.letterIdx = LetterSendList.letterIdx " +
                "WHERE Letter.letterIdx = ? " +
                "AND Letter.status = 'active' " +
                "GROUP BY letterIdx";

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

    // ==============================================================================

    // User.fontIdx 반환
    public int getFontIdx(String type, int idx) {
        // type : Table 명 (Diary / Letter / Reply)
        String typeIdx = type + "Idx"; // 해당 Table의 식별자 명 (Diary : diaryIdx / Letter : letterIdx / Reply : replyIdx)
        String columnName_sender = "userIdx"; // 발신자 Column 명 (Diary & Letter : userIdx / & Reply : replierIdx)

        if (type.compareTo("diary") == 0) { // 일기
            type = "Diary";
        } else if (type.compareTo("letter") == 0) { // 편지
            type = "Letter";
        } else { // 답장
            type = "Reply";
            columnName_sender = "replierIdx";
        }

        String query = "SELECT fontIdx FROM User WHERE userIdx = " +
                "(SELECT " + columnName_sender + " FROM " + type + " WHERE " + typeIdx + " = ? AND status = 'active') " +
                "AND status = 'active'";

        // ex. SELECT fontIdx FROM User WHERE userIdx = (SELECT replierIdx FROM Reply WHERE replierIdx = ? AND status = 'active') AND status = 'active'
        return this.jdbcTemplate.queryForObject(query, int.class, idx);
    }

}
