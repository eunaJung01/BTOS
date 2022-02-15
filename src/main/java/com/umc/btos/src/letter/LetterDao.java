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

    // =================================== 편지 생성 ===================================

    //Letter 테이블에 편지 생성 // 생성한 letterIdx 반환
    public int createLetter(PostLetterReq postLetterReq) {
        // DB의 Letter Table에 (userIdx,content)값을 가지는 편지 데이터를 삽입(생성)
        String createLetterQuery = "insert into Letter (userIdx,content) VALUES (?,?)";
        Object[] createLetterParams = new Object[]{postLetterReq.getUserIdx(), postLetterReq.getContent()};
        this.jdbcTemplate.update(createLetterQuery, createLetterParams);

        // 가장 마지막에 삽입된(생성된) letterIdx값 반환
        String lastInsertIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class);
    }

    //편지를 전송하는 사람의 userIdx와 similarAge의 여부 반환
    public PostLetterUserSimilarIdx getIdx_Similar(int letterIdx) {
        // 편지 전송 유저의 userIdx
        String getUserIdxQuery = "SELECT L.userIdx FROM Letter L WHERE L.letterIdx=?";
        int userIdx = this.jdbcTemplate.queryForObject(getUserIdxQuery, int.class, letterIdx);

        // 편지 발송 유저의 또래 편지 수신 여부
        String getSimilarAgeQuery = "SELECT U.recSimilarAge FROM User U WHERE userIdx=?";
        int userSimilarAge = this.jdbcTemplate.queryForObject(getSimilarAgeQuery, int.class, userIdx);

        //userIdx, 또래 편지 수신 여부를 PostLetterUserSimilarIdx 객체를 만들어 반환
        PostLetterUserSimilarIdx userIdx_SimilarAge = new PostLetterUserSimilarIdx(userIdx, userSimilarAge);
        return userIdx_SimilarAge;
    }

    // [또래유저] 편지를 수신할 유저의 userIdx들 (list형태)로 반환 // +-5살의 유저(또래 유저)만 선택
    public List<Integer> getLetterUserIdx_Similar(PostLetterUserSimilarIdx postLetterUserSimilarIdx) {
        // 편지를 발송하는 유저의 출생년도
        String getBirthQuery = "SELECT U.birth FROM User U WHERE userIdx=?";
        int getUserIdxParam = postLetterUserSimilarIdx.getUserIdx();
        int userBirth = this.jdbcTemplate.queryForObject(getBirthQuery, int.class, getUserIdxParam);

        // 편지를 수신할 유저의 userIdx들
        // 휴먼상태가 아니고, 타인의 편지를 수신하는 유저이고, 나이대가 +-5년의 유저 중 (편지를 보내는 유저 제외) 랜덤으로 5명을 선택
        String getUserIdx = "select U.userIdx from User U where U.status='active' and U.userIdx != ? and U.recOthers = 1 and ( (?-5) <= U.birth and U.birth <=(?+5))  order by rand() limit 5";
        List<Integer> userIdx_Similar = this.jdbcTemplate.queryForList(getUserIdx, int.class, getUserIdxParam, userBirth, userBirth);

        return userIdx_Similar;
    }

    // 편지를 수신할 유저가 5명 이하일 경우 편지를 보낼 유저를 랜덤으로 선택 // 편지를 수신할 유저의 userIdx들 (list형태)로 반환
    public List<Integer> getLetterUserIdx_Random(PostLetterUserSimilarIdx postLetterUserSimilarIdx) { // 편지를 수신할 유저의 userIdx들을 list형태로 반환
        // 휴먼상태가 아니고, 타인의 편지를 수신하는 유저를 list형태로 모두 반환  (편지를 보내는 유저 제외)
        // 랜덤으로 조건에 해당하는 모든 유저의 userIdx를 뽑는 쿼리문
        // 이미 편지를 보낸 유저가 존재할 수 있으므로 넉넉하게 10명을 뽑음
        String getUserIdx = "select U.userIdx from User U where U.status='active' and U.userIdx != ? and U.recOthers = 1 order by rand() limit 10";
        List<Integer> userIdx_Random = this.jdbcTemplate.queryForList(getUserIdx, int.class, postLetterUserSimilarIdx.getUserIdx());

        return userIdx_Random;
    }

    // LetterSendList 테이블에 편지 발송 목록 생성
    public void createLetterSendList(int letterIdx, List<Integer> userIdx_list, int i) {
        String createLetterSendListQuery = "insert into LetterSendList (letterIdx,receiverIdx) VALUES (?,?)";
        Object[] createLetterSendListParams = new Object[]{letterIdx, userIdx_list.get(i)};
        this.jdbcTemplate.update(createLetterSendListQuery, createLetterSendListParams);
    }

    // 편지를 수신할 유저의 userIdx들 랜덤으로 5명 선택하여 (list형태)로 반환
    public List<Integer> getLetterUserIdx(PostLetterUserSimilarIdx postLetterUserSimilarIdx) {
        // 편지를 수신할 유저 선택 // 휴먼상태가 아니고, 타인의 편지를 수신하는 유저 중 (편지를 보내는 유저 제외)
        String getUserIdx = "select U.userIdx from User U where U.status='active' and U.userIdx != ? and recOthers = 1 order by rand() limit 5";
        List<Integer> userIdx_unSimilar = this.jdbcTemplate.queryForList(getUserIdx, int.class, postLetterUserSimilarIdx.getUserIdx());

        return userIdx_unSimilar;
    }

    //편지 삭제 // 해당 letterIdx의 편지 status를 deleted로 변경
    public int modifyLetterStatus(PatchLetterReq patchLetterReq) {
        String modifyLetterStatusQuery = "update Letter set status = ? where letterIdx = ? ";
        Object[] modifyLetterStatusParams = new Object[]{"deleted", patchLetterReq.getLetterIdx()};
        return this.jdbcTemplate.update(modifyLetterStatusQuery, modifyLetterStatusParams);
    }

    // =================================== 우편 조회 - 편지 ===================================

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

    // 해당 userIdx를 갖는 유저의 nickName을 반환
    public String getNickName(int userIdx) {
        String getNickNameQuery = "select nickName from User where userIdx = ?;";
        return this.jdbcTemplate.queryForObject(getNickNameQuery, String.class, userIdx);
    }

    //편지 조회 여부 변경 // 해당 letterIdx를 갖는 편지의 isChecked를 1로 update
    public int modifyIsChecked(int letterIdx, int receiverIdx) {
        String getReplyQuery = "UPDATE LetterSendList SET isChecked = 1 WHERE letterIdx = ? AND receiverIdx = ?";
        return this.jdbcTemplate.update(getReplyQuery, letterIdx, receiverIdx); // 대응시켜 매핑시켜 쿼리 요청 (성공했으면 1, 실패했으면 0)
    }

    // 형식적 validation - 회원 존재 여부 확인
    public int checkUserIdx(int userIdx) {
        String query = "SELECT EXISTS (SELECT userIdx FROM User WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

}
