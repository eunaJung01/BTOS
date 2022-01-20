package com.umc.btos.src.letter;


import com.umc.btos.src.letter.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Repository
public class LetterDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    // 편지 생성
    public int createLetter(PostLetterReq postLetterReq) {
        String createLetterQuery = "insert into Letter (replier,receiver,content) VALUES (?,?,?)"; // 실행될 동적 쿼리문
        Object[] createLetterParams = new Object[]{postLetterReq.getReplier(),postLetterReq.getReceiver(),postLetterReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createLetterQuery, createLetterParams);

        // 즉 DB의 Letter Table에 (replier,receiver,content)값을 가지는 편지 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }

    // 해당 letterIdx를 갖는 편지조회
    public GetLetterRes getLetter(int letterIdx) {
        String getLetterQuery = "select * from Letter where letterIdx = ?"; // 해당 letterIdx를 만족하는 편지를 조회하는 쿼리문
        int getLetterParams = letterIdx;
        return this.jdbcTemplate.queryForObject(getLetterQuery,
                (rs, rowNum) -> new GetLetterRes(
                        rs.getInt("letterIdx"),
                        rs.getInt("replier"),
                        rs.getInt("receiver"),
                        rs.getString("content")), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getLetterParams); // 한 개의 편지정보를 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }
    // 편지 status 변경
    public int modifyLetterStatus(PatchLetterReq patchLetterReq) {
        String modifyLetterStatusQuery = "update Letter set status = ? where letterIdx = ? "; // 해당 userIdx를 만족하는 User를 해당 nickname으로 변경한다.
        Object[] modifyLetterStatusParams = new Object[]{"deleted", patchLetterReq.getLetterIdx()}; // 주입될 값들(status, letterIdx) 순

        return this.jdbcTemplate.update(modifyLetterStatusQuery, modifyLetterStatusParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

}
