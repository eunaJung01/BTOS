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

    // ============================================== 편지 저장 및 발송 ===============================================

    // 편지 저장
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
        String query = "SELECT birth FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, senderUserIdx);
    }

    // 회원마다 가장 최근에 받은 편지 letterIdx 반환
    public int getUserIdx_recentReceived(int userIdx) {
        String query = "SELECT userIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "WHERE receiverIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "ORDER BY LetterSendList.createdAt DESC " +
                "LIMIT 1"; // 상위 첫번째 값

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 발송 가능한 회원들의 목록
    public List<User> getUserList(int senderUserIdx) {
        String query = "SELECT userIdx, birth, recSimilarAge " +
                "FROM User " +
                "WHERE userIdx != ? " +
                "  AND recOthers = 1 " +
                "  AND status = 'active'";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new User(
                        rs.getInt("userIdx"),
                        rs.getInt("birth"),
                        rs.getInt("recSimilarAge")
                ), senderUserIdx);
    }

    // 편지 수신 유무
    public int hasReceivedLetter(int userIdx) {
        String query = "SELECT EXISTS (SELECT * FROM LetterSendList WHERE receiverIdx = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 발송
    public int sendLetter(int letterIdx, int receiverIdx) {
        String query = "INSERT LetterSendList (letterIdx, receiverIdx) VALUE(?,?)";
        return this.jdbcTemplate.update(query, letterIdx, receiverIdx);
    }

    // 수신인 User.fcmToken 반환
    public String getFcmToken(int userIdx) {
        String query = "SELECT fcmToken FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

    // ================================================================================================================


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
