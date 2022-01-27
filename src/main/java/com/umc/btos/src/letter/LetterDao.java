package com.umc.btos.src.letter;


import com.umc.btos.src.letter.model.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;


@EnableScheduling // 추가
@Repository
public class LetterDao {

    private JdbcTemplate jdbcTemplate;
    private int repeatTime =0;
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
    // 편지 생성
    public int createLetter(PostLetterReq postLetterReq) {
        String createLetterQuery = "insert into Letter (userIdx,content) VALUES (?,?)"; // 실행될 동적 쿼리문
        Object[] createLetterParams = new Object[]{postLetterReq.getUserIdx(),postLetterReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createLetterQuery, createLetterParams);

        // 즉 DB의 Letter Table에 (replierIdx,receiverIdx,content)값을 가지는 편지 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 letterIdx번호를 반환한다.
    }

    /**
     * 해당 메서드 로직이 끝나는 시간 기준, milliseconds 간격으로 실행
     *  이전 작업이 완료될 때까지 대기

    @Autowired
    private TaskScheduler taskScheduler;
    // 편지 발송
    @Async // 병렬로 Scheduler 를 사용할 경우 @Async 추가
    @Scheduled(fixedDelay = 21600000) // 6시간 간격으로 메서드 수행
    public void sendLetter(PostLetterSendListReq postLetterSendListReq) {
        String sendLetterQuery = "insert into LetterSendList (letterIdx,receiverIdx) VALUES (?,?)"; // 실행될 동적 쿼리문
        Object[] sendLetterParams = new Object[]{postLetterSendListReq.getLetterIdx(),postLetterSendListReq.getReceiverIdx()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(sendLetterQuery, sendLetterParams);
        repeatTime+= 1;
        if (repeatTime==5){

        }
    }
    */
    /**
    // 해당 letterIdx를 갖는 편지조회
    public GetLetterRes getLetter(int letterIdx) {
        String getLetterQuery = "select * from Letter where letterIdx = ?"; // 해당 letterIdx를 만족하는 편지를 조회하는 쿼리문
        int getLetterParams = letterIdx;
        return this.jdbcTemplate.queryForObject(getLetterQuery,
                (rs, rowNum) -> new GetLetterRes(
                        rs.getInt("letterIdx"),
                        rs.getInt("replierIdx"),
                        rs.getInt("receiverIdx"),
                        rs.getString("content")),
                getLetterParams); // 한 개의 편지정보를 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

     */
    /**
    //편지 삭제 // 해당 letterIdx의 편지 status를 deleted로 변경
    public int modifyLetterStatus(PatchLetterReq patchLetterReq) {
        String modifyLetterStatusQuery = "update Letter set status = ? where letterIdx = ? "; // 해당 userIdx를 만족하는 User를 해당 nickname으로 변경한다.
        Object[] modifyLetterStatusParams = new Object[]{"deleted", patchLetterReq.getLetterIdx()}; // 주입될 값들(status, letterIdx) 순

        return this.jdbcTemplate.update(modifyLetterStatusQuery, modifyLetterStatusParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }*/

}
