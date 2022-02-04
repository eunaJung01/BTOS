package com.umc.btos.src.reply;



import com.umc.btos.src.letter.model.GetLetterRes;
import com.umc.btos.src.letter.model.PatchLetterReq;
import com.umc.btos.src.reply.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class ReplyDao {

    private JdbcTemplate jdbcTemplate;


    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    // 답장 생성
    public int createReply(PostReplyReq postReplyReq) {
        String createReplyQuery = "insert into Reply (replierIdx,receiverIdx,firstHistoryType,sendIdx,content) VALUES (?,?,?,?,?)"; // 실행될 동적 쿼리문
        Object[] createReplyParams = new Object[]{postReplyReq.getReplierIdx(),postReplyReq.getReceiverIdx(),postReplyReq.getFirstHistoryType(),postReplyReq.getSendIdx(),postReplyReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createReplyQuery, createReplyParams);

        // 즉 DB의 Letter Table에 (replier,receiver,content)값을 가지는 편지 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }

    // 답장 status 변경
    public int modifyReplyStatus(PatchReplyReq patchReplyReq) {
        String modifyReplyStatusQuery = "update Reply set status = ? where replyIdx = ? "; // 해당 userIdx를 만족하는 User를 해당 nickname으로 변경한다.
        Object[] modifyReplyStatusParams = new Object[]{"deleted", patchReplyReq.getReplyIdx()}; // 주입될 값들(status, letterIdx) 순

        return this.jdbcTemplate.update(modifyReplyStatusQuery, modifyReplyStatusParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

    // =================================== 우편 조회 - 답장 ===================================

    // 해당 replyIdx를 갖는 답장 조회
    public GetReplyRes getReply(int replyIdx) {
        String getReplyQuery = "SELECT replyIdx, content FROM Reply WHERE replyIdx = ?";

        return this.jdbcTemplate.queryForObject(getReplyQuery,
                (rs, rowNum) -> new GetReplyRes(
                        rs.getInt("replyIdx"),
                        rs.getString("content")),
                replyIdx);
    }

    // 해당 replyIdx를 갖는 답장의 isChecked를 1로 update
    public int modifyIsChecked(int replyIdx) {
        String getReplyQuery = "UPDATE Reply SET isChecked = 1 WHERE replyIdx = ? "; // 해당 replyIdx를 만족하는 답장의 열람 여부를 변경하는 쿼리문
        return this.jdbcTemplate.update(getReplyQuery, replyIdx); // 대응시켜 매핑시켜 쿼리 요청(선공했으면 1, 실패했으면 0)
    }

}
