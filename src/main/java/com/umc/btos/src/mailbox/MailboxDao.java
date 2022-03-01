package com.umc.btos.src.mailbox;

import com.umc.btos.src.diary.model.Done;
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

    // 존재하는 회원인지 확인
    public int checkUserIdx(int userIdx) {
        String query = "SELECT EXISTS (SELECT userIdx FROM User WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 존재하는 일기인지 확인
    public int checkDiaryIdx(int diaryIdx) {
        String query = "SELECT EXISTS (SELECT diaryIdx FROM Diary WHERE diaryIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 존재하는 편지인지 확인
    public int checkLetterIdx(int letterIdx) {
        String query = "SELECT EXISTS (SELECT letterIdx FROM Letter WHERE letterIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, letterIdx);
    }

    // 존재하는 답장인지 확인
    public int checkReplyIdx(int replyIdx) {
        String query = "SELECT EXISTS (SELECT replyIdx FROM Reply WHERE replyIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
    }

    // 해당 회원이 받은 일기인지 확인
    public int checkUserAboutMail_diary(int receiverIdx, int diaryIdx) {
        String query = "SELECT EXISTS (SELECT sendIdx FROM DiarySendList WHERE receiverIdx = ? AND diaryIdx = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, receiverIdx, diaryIdx);
    }

    // 해당 회원이 받은 편지인지 확인
    public int checkUserAboutMail_letter(int receiverIdx, int letterIdx) {
        String query = "SELECT EXISTS (SELECT sendIdx FROM LetterSendList WHERE receiverIdx = ? AND letterIdx = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, receiverIdx, letterIdx);
    }

    // 해당 회원이 받은 답장인지 확인
    public int checkUserAboutMail_reply(int receiverIdx, int replyIdx) {
        String query = "SELECT EXISTS (SELECT replyIdx FROM Reply WHERE receiverIdx = ? AND replyIdx = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, receiverIdx, replyIdx);
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

    // type = diary
    public GetMailRes getMail_diary(int receiverIdx, int diaryIdx) {
        String query = "SELECT Diary.content, " +
                "       Diary.emotionIdx, " +
                "       Diary.diaryDate AS sendAt, " +
//                "       date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                                     AS senderIdx, " +
                "       User.nickName                                    AS senderNickName, " +
                "       User.fontIdx                                     AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.diaryIdx = ?";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetMailRes(
                        "diary",
                        "diary",
                        diaryIdx,
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
//                        rs.getString("diaryDate"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), receiverIdx, diaryIdx);
    }

    // done list 유무 반환
    public boolean hasDoneList(int diaryIdx) {
        String query = "SELECT EXISTS (SELECT COUNT(*) FROM Done WHERE diaryIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, boolean.class, diaryIdx);
    }

    // done list
    public List<String> getDoneList(int diaryIdx) {
        String query = "SELECT content FROM Done WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForList(query, String.class, diaryIdx);
    }

    // type = letter
    public GetMailRes getMail_letter(int receiverIdx, int letterIdx) {
        String query = "SELECT Letter.letterIdx, " +
                "       Letter.content, " +
                "       date_format(LetterSendList.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                                      AS senderIdx, " +
                "       User.nickName                                     AS senderNickName, " +
                "       User.fontIdx                                      AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "  AND Letter.letterIdx = ?";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetMailRes(
                        "letter",
                        "letter",
                        letterIdx,
                        rs.getString("content"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), receiverIdx, letterIdx);
    }

    // type = reply
    public GetMailRes getMail_reply(int receiverIdx, int replyIdx) {
        String query = "SELECT Reply.firstHistoryType, " +
                "       Reply.replyIdx, " +
                "       Reply.content, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "  AND Reply.replyIdx = ?";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetMailRes(
                        rs.getString("firstHistoryType"),
                        "reply",
                        replyIdx,
                        rs.getString("content"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), receiverIdx, replyIdx);
    }

    // ------------------------------------ modifyIsChecked ------------------------------------

    // DiarySendList.isChecked = 1로 변환
    public void modifyIsChecked_diary(int receiverIdx, int diaryIdx) {
        String query = "UPDATE DiarySendList SET isChecked = 1 WHERE receiverIdx = ? AND diaryIdx = ?";
        this.jdbcTemplate.update(query, receiverIdx, diaryIdx);
    }

    // LetterSendList.isChecked : 0 -> 1
    public void modifyIsChecked_letter(int receiverIdx, int letterIdx) {
        String query = "UPDATE LetterSendList SET isChecked = 1 WHERE receiverIdx = ? AND letterIdx = ?";
        this.jdbcTemplate.update(query, receiverIdx, letterIdx);
    }

    // Reply.isChecked : 0 -> 1
    public void modifyIsChecked_reply(int receiverIdx, int replyIdx) {
        String query = "UPDATE Reply SET isChecked = 1 WHERE receiverIdx = ? AND replyIdx = ?";
        this.jdbcTemplate.update(query, receiverIdx, replyIdx);
    }

    // -------------------------------------------------------------------------------------------

    // 발신인 계정 상태 반환
    public boolean getSenderActive(String type, int typeIdx) {
        String query = "";

        switch (type) {
            case ("diary"):
                query = "SELECT User.status " +
                        "FROM DiarySendList " +
                        "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                        "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                        "WHERE Diary.diaryIdx = ? " +
                        "  AND Diary.isSend = 1 " +
                        "  AND DiarySendList.status = 'active' " +
                        "GROUP BY User.status";
                break;

            case ("letter"):
                query = "SELECT User.status " +
                        "FROM LetterSendList " +
                        "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                        "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                        "WHERE LetterSendList.letterIdx = ? " +
                        "  AND LetterSendList.status = 'active' " +
                        "GROUP BY User.status";
                break;

            case ("reply"):
                query = "SELECT User.status " +
                        "FROM Reply " +
                        "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                        "WHERE Reply.replyIdx = ? " +
                        "  AND Reply.status = 'active'";
                break;
        }

        String senderStatus = this.jdbcTemplate.queryForObject(query, String.class, typeIdx);
        return senderStatus.compareTo("deleted") != 0; // User.status = delete -> false
    }

}
