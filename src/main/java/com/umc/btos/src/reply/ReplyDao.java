package com.umc.btos.src.reply;



import com.umc.btos.src.letter.model.GetLetterRes;
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
        String createReplyQuery = "insert into Reply (replierIdx,receiverIdx,firstType,content) VALUES (?,?,?,?)"; // 실행될 동적 쿼리문
        Object[] createReplyParams = new Object[]{postReplyReq.getReplierIdx(),postReplyReq.getReceiverIdx(),postReplyReq.getFirstType(),postReplyReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createReplyQuery, createReplyParams);

        // 즉 DB의 Letter Table에 (replier,receiver,content)값을 가지는 편지 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }
    // 해당 replyIdx를 갖는 답장 조회
    public GetReplyRes getReply(int replyIdx) {
        String getReplyQuery = "select * from Reply where replyIdx = ?"; // 해당 letterIdx를 만족하는 편지를 조회하는 쿼리문
        int getReplyParams = replyIdx;
        return this.jdbcTemplate.queryForObject(getReplyQuery,
                (rs, rowNum) -> new GetReplyRes(
                        rs.getInt("replyIdx"),
                        rs.getInt("replierIdx"),
                        rs.getInt("receiverIdx"),
                        rs.getString("firstType"),
                        rs.getString("content")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getReplyParams); // 한 개의 편지정보를 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }
}
