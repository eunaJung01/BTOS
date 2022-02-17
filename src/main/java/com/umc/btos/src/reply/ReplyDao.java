package com.umc.btos.src.reply;

import com.umc.btos.src.reply.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class ReplyDao {

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

    // 답장 존재 여부 확인
    public int checkReplyIdx(int replyIdx) {
        String query = "SELECT EXISTS (SELECT replyIdx FROM Reply WHERE replyIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
    }

    // 해당 회원이 작성한 답장인지 확인
    public int checkUserAboutReply(int userIdx, int replyIdx) {
        String query = "SELECT EXISTS (SELECT replyIdx FROM Reply WHERE userIdx = ? AND replyIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, replyIdx);
    }

    // ============================================== 답장 저장 및 발송 ===============================================

    // 답장 저장
    public int postReply(PostReplyReq postReplyReq) {
        String query = "INSERT INTO Reply (replierIdx, receiverIdx, firstHistoryType, sendIdx, content) VALUES (?,?,?,?,?)";
        Object[] params = new Object[]{postReplyReq.getReplierIdx(), postReplyReq.getReceiverIdx(), postReplyReq.getFirstHistoryType(), postReplyReq.getSendIdx(), postReplyReq.getContent()};
        this.jdbcTemplate.update(query, params);

        // replyIdx 반환
        String query_getReplyIdx = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(query_getReplyIdx, int.class);
    }

    // 발신인 User.nickName 반환
    public String getNickName(int replierIdx) {
        String query = "SELECT nickName FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, replierIdx);
    }

    // 수신인 fcmToken 반환
    public String getFcmToken(int senderIdx) {
        String query = "SELECT fcmToken FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, senderIdx);
    }

    // ================================================== 답장 삭제 ===================================================

    // Reply.status : active -> deleted
    public int deleteReply(int replyIdx) {
        String query = "UPDATE Reply SET status = 'deleted' WHERE replyIdx = ?";
        return this.jdbcTemplate.update(query, replyIdx);
    }

    // =============================================== 우편 조회 - 답장 ===============================================

    // 해당 replyIdx를 갖는 답장 조회
    public GetReplyRes getReply(int replyIdx) {
        String getReplyQuery = "SELECT replyIdx, content FROM Reply WHERE replyIdx = ?";

        return this.jdbcTemplate.queryForObject(getReplyQuery,
                (rs, rowNum) -> new GetReplyRes(
                        rs.getInt("replyIdx"),
                        rs.getString("content")),
                replyIdx);
    }

    // 답장 열람 여부 // 해당 replyIdx를 갖는 답장의 isChecked를 1로 update
    public int modifyIsChecked(int replyIdx) {
        String getReplyQuery = "UPDATE Reply SET isChecked = 1 WHERE replyIdx = ? ";
        return this.jdbcTemplate.update(getReplyQuery, replyIdx);
    }

}
