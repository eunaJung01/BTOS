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
        String query = "SELECT EXISTS (SELECT replyIdx FROM Reply WHERE replierIdx = ? AND replyIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, replyIdx);
    }

    // ============================================== 답장 저장 및 발송 ===============================================

    // 답장 저장
    public int postReply(PostReplyReq postReplyReq) {
        String firstHistoryType = postReplyReq.getFirstHistoryType();
        int sendIdx = postReplyReq.getSendIdx();

        // History에서 답장하는 경우 (firstHistoryType == "reply")
//        if (firstHistoryType.compareTo("reply") == 0) {
//            do {
//                String get_firstHistoryType_query = "select firstHistoryType from Reply where replyIdx = ?";
//                firstHistoryType = this.jdbcTemplate.queryForObject(get_firstHistoryType_query, String.class, sendIdx);
//
//                String get_sendIdx_query = "select sendIdx from Reply where replyIdx = ?";
//                sendIdx = this.jdbcTemplate.queryForObject(get_sendIdx_query, int.class, sendIdx);
//            } while (firstHistoryType.compareTo("diary") != 0 && firstHistoryType.compareTo("letter") != 0);
//            firstHistoryType = "reply";
//        }

        String query = "INSERT INTO Reply (replierIdx, receiverIdx, firstHistoryType, sendIdx, content) VALUES (?,?,?,?,?)";
        Object[] params = new Object[]{postReplyReq.getReplierIdx(), postReplyReq.getReceiverIdx(), firstHistoryType, sendIdx, postReplyReq.getContent()};
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

    // 답장 발신인이 푸시 알림을 수신 허용인지 확인
    public int checkPushAlarm(int receiverIdx) {
        String query = "SELECT exists(SELECT userIdx FROM User WHERE userIdx = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, receiverIdx);
    }

    // =============================================== 우편 조회 - 답장 ===============================================

    // 답장 조회
//    public GetReplyRes getReply(int replyIdx) {
//        String query = "SELECT replyIdx, content FROM Reply WHERE replyIdx = ?";
//
//        return this.jdbcTemplate.queryForObject(query,
//                (rs, rowNum) -> new GetReplyRes(
//                        rs.getInt("replyIdx"),
//                        rs.getString("content")),
//                replyIdx);
//    }
//
//    // Reply.isChecked : 0 -> 1
//    public int modifyIsChecked(int replyIdx) {
//        String query = "UPDATE Reply SET isChecked = 1 WHERE replyIdx = ?";
//        return this.jdbcTemplate.update(query, replyIdx);
//    }

}
