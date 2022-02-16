package com.umc.btos.src.letter;

import com.umc.btos.src.letter.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class LetterDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // ================================================== validation ==================================================

    // 회원 존재 여부 확인
    public int checkUserIdx(int userIdx) {
        String query = "SELECT EXISTS (SELECT userIdx FROM User WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 존재 여부 확인
    public int checkLetterIdx(int letterIdx) {
        String query = "SELECT EXISTS (SELECT letterIdx FROM Letter WHERE letterIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, letterIdx);
    }

    // 해당 회원이 작성한 편지인지 확인
    public int checkUserAboutLetter(int userIdx, int letterIdx) {
        String query = "SELECT EXISTS (SELECT letterIdx FROM Letter WHERE userIdx = ? AND letterIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, letterIdx);
    }

    // ============================================== 편지 저장 및 발송 ===============================================

    // 편지 저장 (INSERT Letter)
    public int postLetter(PostLetterReq postLetterReq) {
        String query = "INSERT INTO Letter (userIdx,content) VALUES (?,?)";
        this.jdbcTemplate.update(query, postLetterReq.getUserIdx(), postLetterReq.getContent());

        // letterIdx 반환
        String query_getLetterIdx = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(query_getLetterIdx, int.class);
    }

    // 발신인 User.nickName 반환
    public String getNickName(int userIdx) {
        String query = "SELECT nickName FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

    // 발신인 User.birth 반환
    public int getSenderBirth(int senderUserIdx) {
        String query = "SELECT CASE " +
                "           WHEN (SELECT birth FROM User WHERE userIdx = ?) IS NULL " + // User.birth == null
                "               THEN " +
                "                   (SELECT 0) " + // 0 반환
                "           WHEN (SELECT birth FROM User WHERE userIdx = ?) IS NOT NULL " + // User.birth != null
                "               THEN " +
                "                   (SELECT birth FROM User WHERE userIdx = ?) " + // User.birth 반환
                "           END";

        return this.jdbcTemplate.queryForObject(query, int.class, senderUserIdx, senderUserIdx, senderUserIdx);
    }

    // 회원마다 가장 최근에 받은 편지 letterIdx 반환
    public int getUserIdx_recentReceived(int userIdx) {
        String query = "SELECT userIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "WHERE receiverIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "ORDER BY LetterSendList.createdAt DESC " + // createdAt 기준 내림차순 정렬
                "LIMIT 1"; // 상위 첫번째 값

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 발송 가능한 회원들의 목록
    public List<User> getUserList(int senderUserIdx) {
        String query = "SELECT userIdx, birth, recSimilarAge " +
                "FROM User " +
                "WHERE userIdx != ? " + // 발신인 제외
                "  AND recOthers = 1 " +
                "  AND status = 'active'";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new User(
                        rs.getInt("userIdx"),
                        rs.getInt("birth"),
                        rs.getInt("recSimilarAge")
                ), senderUserIdx);
    }

    // 편지 수신 유무 반환
    public int hasReceivedLetter(int userIdx) {
        String query = "SELECT EXISTS (SELECT * FROM LetterSendList WHERE receiverIdx = ?)"; // 편지를 수신한 적이 있다면 1, 없다면 0 반환
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 발송 (INSERT LetterSendList)
    public void sendLetter(int letterIdx, int receiverIdx) {
        String query = "INSERT LetterSendList (letterIdx, receiverIdx) VALUE(?,?)";
        this.jdbcTemplate.update(query, letterIdx, receiverIdx);
    }

    // 수신인 User.fcmToken 반환
    public String getFcmToken(int userIdx) {
        String query = "SELECT fcmToken FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

    // ================================================== 편지 삭제 ===================================================

    // Letter.status : active -> deleted
    public int deleteLetter(int letterIdx) {
        String query = "UPDATE Letter SET status = 'deleted' WHERE letterIdx = ?";
        return this.jdbcTemplate.update(query, letterIdx);
    }

    // =============================================== 우편 조회 - 편지 ===============================================

    // 해당 letterIdx를 갖는 편지조회
    public GetLetterRes getLetter(int letterIdx, int receiverIdx) {
        String getLetterQuery = "SELECT Letter.letterIdx, Letter.content " +
                "FROM Letter " +
                "INNER JOIN LetterSendList ON Letter.letterIdx = LetterSendList.letterIdx " +
                "WHERE Letter.letterIdx = ? " +
                "AND LetterSendList.receiverIdx = ?";

        return this.jdbcTemplate.queryForObject(getLetterQuery,
                (rs, rowNum) -> new GetLetterRes(
                        rs.getInt("letterIdx"),
                        rs.getString("content")),
                letterIdx, receiverIdx);
    }

    // LetterSendList.isChecked : 0 -> 1
    public int modifyIsChecked(int letterIdx, int receiverIdx) {
        String query = "UPDATE LetterSendList SET isChecked = 1 WHERE letterIdx = ? AND receiverIdx = ?";
        return this.jdbcTemplate.update(query, letterIdx, receiverIdx);
    }

}
