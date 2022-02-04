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
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;


@EnableScheduling // 추가
@Repository
public class LetterDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    // 편지 생성
    public int createLetter(PostLetterReq postLetterReq) { // 생성한 letterIdx 반환
        String createLetterQuery = "insert into Letter (userIdx,content) VALUES (?,?)"; // 실행될 동적 쿼리문
        Object[] createLetterParams = new Object[]{postLetterReq.getUserIdx(), postLetterReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createLetterQuery, createLetterParams);
        // 즉 DB의 Letter Table에 (userIdx,content)값을 가지는 편지 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 letterIdx번호를 반환한다.
    }

    // 편지 발송 // letterSendList에 isSad상태가 아닌 userIdx 5개를 골라 SendList에 컬럼 추가하는 함수
    public List<Integer> createLetterSendList(int letterIdx) { // 생성된 letter의 Idx를 인수로 받는다.
        //해당 편지를 발송한 userIdx
        String getUserIdxQuery = "SELECT L.userIdx FROM Letter L WHERE L.letterIdx=?";
        int getUserIdxParam = letterIdx;
        int userIdx = this.jdbcTemplate.queryForObject(getUserIdxQuery, int.class, getUserIdxParam);
        // 편지 발송 유저의 또래 편지 수신 여부
        String getSimilarAgeQuery = "SELECT U.recSimilarAge FROM User U WHERE userIdx=?";
        int userSimilarAge = this.jdbcTemplate.queryForObject(getSimilarAgeQuery, int.class, userIdx);

        if (userSimilarAge == 1) { // 편지 발송 유저가 또래 편지 수신을 원할경우

            String getBirthQuery = "SELECT U.birth FROM User U WHERE userIdx=?"; // 해당 유저의 생년을 구한다.
            int userBirth = this.jdbcTemplate.queryForObject(getBirthQuery, int.class, userIdx);

            String getUserIdx = "select U.userIdx from User U where U.status='active' and U.recOthers = 1 and ( (?-5) <= U.birth and U.birth <=(?+5))  order by rand() limit 5";
            // 휴먼상태가 아니고, 타인의 편지를 수신하는 유저이고, 나이대가 +-5년의 유저 중
            // 랜덤으로 5명의 userIdx를 뽑는 쿼리문
            List<Integer> userIdx_Similar = this.jdbcTemplate.queryForList(getUserIdx, int.class, userBirth, userBirth); // 변수 userIdx에 랜덤으로 뽑은 userIdx를 넣는다.

            for (int i = 0; i < 5; i++) { // 5명의 userIdx를 뽑는다. // 1명씩 테이블에 추가하므로 5번 반복
                String createLetterSendListQuery = "insert into LetterSendList (letterIdx,receiverIdx) VALUES (?,?)"; // 실행될 동적 쿼리문
                Object[] createLetterSendListParams = new Object[]{letterIdx, userIdx_Similar.get(i)}; // 동적 쿼리의 ?부분에 주입될 값
                this.jdbcTemplate.update(createLetterSendListQuery, createLetterSendListParams);
            }
            return userIdx_Similar;
        } else { //편지 발송 유저가 또래 편지 수신을 원하지 않을경우
            String getUserIdx = "select U.userIdx from User U where U.status='active' and recOthers = 1 order by rand() limit 5";
            // 휴먼상태가 아니고, 타인의 편지를 수신하는 유저 중
            // 랜덤으로 5명의 userIdx를 뽑는 쿼리문
            List<Integer> userIdx_unSimilar = this.jdbcTemplate.queryForList(getUserIdx, int.class); // 변수 userIdx에 랜덤으로 뽑은 userIdx를 넣는다.

            for (int i = 0; i < 5; i++) { // 5명의 userIdx를 뽑는다. // 1명씩 테이블에 추가하므로 5번 반복
                String createLetterSendListQuery = "insert into LetterSendList (letterIdx,receiverIdx) VALUES (?,?)"; // 실행될 동적 쿼리문
                Object[] createLetterSendListParams = new Object[]{letterIdx, userIdx_unSimilar.get(i)}; // 동적 쿼리의 ?부분에 주입될 값
                this.jdbcTemplate.update(createLetterSendListQuery, createLetterSendListParams);
            }
            return userIdx_unSimilar;
        }

    }

    //편지 삭제 // 해당 letterIdx의 편지 status를 deleted로 변경
    public int modifyLetterStatus(PatchLetterReq patchLetterReq) {
        String modifyLetterStatusQuery = "update Letter set status = ? where letterIdx = ? "; // 해당 userIdx를 만족하는 User를 해당 nickname으로 변경한다.
        Object[] modifyLetterStatusParams = new Object[]{"deleted", patchLetterReq.getLetterIdx()}; // 주입될 값들(status, letterIdx) 순

        return this.jdbcTemplate.update(modifyLetterStatusQuery, modifyLetterStatusParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

    // =================================== 우편 조회 - 답장 ===================================

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

    // 해당 letterIdx를 갖는 편지의 isChecked를 1로 update
    public int modifyIsChecked(int letterIdx, int receiverIdx) {
        String getReplyQuery = "UPDATE LetterSendList SET isChecked = 1 WHERE letterIdx = ? AND receiverIdx = ?";
        return this.jdbcTemplate.update(getReplyQuery, letterIdx, receiverIdx); // 대응시켜 매핑시켜 쿼리 요청 (성공했으면 1, 실패했으면 0)
    }

}
