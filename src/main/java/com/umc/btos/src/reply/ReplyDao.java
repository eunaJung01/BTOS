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

    // 답장 생성 // replyIdx를 반환
    public int createReply(PostReplyReq postReplyReq) {
        // DB의 Reply Table에 (replierIdx,receiverIdx,firstHistoryType,sendIdx,content)값을 가지는 답장 데이터를 삽입(생성)한다.
        String createReplyQuery = "insert into Reply (replierIdx,receiverIdx,firstHistoryType,sendIdx,content) VALUES (?,?,?,?,?)";
        Object[] createReplyParams = new Object[]{postReplyReq.getReplierIdx(), postReplyReq.getReceiverIdx(), postReplyReq.getFirstHistoryType(), postReplyReq.getSendIdx(), postReplyReq.getContent()};
        this.jdbcTemplate.update(createReplyQuery, createReplyParams);

        // 가장 마지막에 생성된 replyIdx
        String lastInsertIdQuery = "select last_insert_id()";
        int replyIdx = this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class);
        return replyIdx;
    }

    // 답장을 보내는 유저의 닉네임 반환
    public String getNickname(int userIdx) {
        String getNickNameQuery = "select nickName from User where userIdx = ?; ";
        return this.jdbcTemplate.queryForObject(getNickNameQuery, String.class, userIdx);
    }

    // 답장 제거 // 답장 status 변경 : active -> deleted
    public int modifyReplyStatus(PatchReplyReq patchReplyReq) {
        String modifyReplyStatusQuery = "update Reply set status = ? where replyIdx = ? ";
        Object[] modifyReplyStatusParams = new Object[]{"deleted", patchReplyReq.getReplyIdx()};
        return this.jdbcTemplate.update(modifyReplyStatusQuery, modifyReplyStatusParams);
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
