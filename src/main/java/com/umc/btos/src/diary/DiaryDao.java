package com.umc.btos.src.diary;

import com.umc.btos.config.Constant;
import com.umc.btos.src.diary.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DiaryDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDateSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 해당 날짜에 일기 작성 여부 확인 (1 : 작성함, 0 : 작성 안 함)
    public int checkDiaryDate(int userIdx, String date) {
        String query = "SELECT EXISTS (SELECT diaryDate FROM Diary WHERE userIdx = ? AND diaryDate = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, date);
    }

    // 회원 확인 (존재 유무, status)
    public int checkUserIdx(int userIdx) {
        String query = "SELECT EXISTS (SELECT userIdx FROM User WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 일기 확인 (존재 유무, status)
    public int checkDiaryIdx(int diaryIdx) {
        String query = "SELECT EXISTS (SELECT diaryIdx FROM Diary WHERE diaryIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 해당 회원이 작성한 일기인지 확인
    public int checkUserAboutDiary(int userIdx, int diaryIdx) {
        String query = "SELECT EXISTS (SELECT diaryIdx FROM Diary WHERE userIdx = ? AND diaryIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, diaryIdx);
    }

    // =================================== 일기 저장 ===================================

    // 일기 저장 -> diaryIdx 반환
    public int saveDiary(PostDiaryReq postDiaryReq) {
        String query = "INSERT INTO Diary(userIdx, emotionIdx, diaryDate, isPublic, content) VALUES(?,?,?,?,?)";
        Object[] diary = new Object[]{
                postDiaryReq.getUserIdx(), postDiaryReq.getEmotionIdx(), postDiaryReq.getDiaryDate(), postDiaryReq.getIsPublic_int(), postDiaryReq.getDiaryContent()
        };
        this.jdbcTemplate.update(query, diary);

        String get_diaryIdx_query = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(get_diaryIdx_query, int.class);
    }

    // done list 저장
    public void saveDoneList(int diaryIdx, List doneList) {
        String query = "INSERT INTO Done(diaryIdx, content) VALUES(?,?)";

        for (Object doneContent : doneList) {
            Object[] done = new Object[]{
                    diaryIdx, doneContent
            };
            this.jdbcTemplate.update(query, done); // Done Table에 순차적으로 저장
        }
    }

    // =================================== 일기 수정 ===================================

    // diaryDate 반환
    public String getDiaryDate(int diaryIdx) {
        String query = "SELECT diaryDate FROM Diary WHERE diaryIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
    }

    // 일기 수정
    public int modifyDiary(PutDiaryReq putDiaryReq) {
        String query = "UPDATE Diary SET emotionIdx = ?, diaryDate = ?, isPublic = ?, content = ? WHERE diaryIdx = ?";
        Object[] params = new Object[]{putDiaryReq.getEmotionIdx(), putDiaryReq.getDiaryDate(), putDiaryReq.getIsPublic_int(), putDiaryReq.getDiaryContent(), putDiaryReq.getDiaryIdx()};
        return this.jdbcTemplate.update(query, params);
    }

    // 해당 일기의 모든 doneIdx를 List 형태로 반환
    public List<Integer> getDoneIdxList(PutDiaryReq putDiaryReq) {
        String query = "SELECT doneIdx FROM Done WHERE diaryIdx = ?";
        return this.jdbcTemplate.queryForList(query, int.class, putDiaryReq.getDiaryIdx());
    }

    // done 수정 - UPDATE
    public int modifyDone(int doneIdx, String doneContent) {
        String query = "UPDATE Done SET content = ?, status = 'active' WHERE doneIdx = ?";
        return this.jdbcTemplate.update(query, doneContent, doneIdx);
    }

    // done 수정 - INSERT
    public int modifyDone_insert(int diaryIdx, String doneContent) {
        String query = "INSERT INTO Done(diaryIdx, content) VALUE(?,?)";
        return this.jdbcTemplate.update(query, diaryIdx, doneContent);
    }

    // done 수정 - status = 'deleted'
    public int modifyDone_modifyStatus(int doneIdx) {
        String query = "UPDATE Done SET status = 'deleted' WHERE doneIdx = ?";
        return this.jdbcTemplate.update(query, doneIdx);
    }

    // =================================== 일기 삭제 ===================================

    // 일기 삭제 - Diary.status : active -> deleted
    public int deleteDiary(int diaryIdx) {
        String query = "UPDATE Diary SET status = 'deleted' WHERE diaryIdx = ?";
        return this.jdbcTemplate.update(query, diaryIdx);
    }

    // done list 유무 반환
    public int hasDone(int diaryIdx) {
        String query = "SELECT EXISTS (SELECT * FROM Done WHERE diaryIdx = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx); // 존재하면 1, 존재하지 않으면 0 반환
    }

    // done list 삭제 - Done.status : active -> deleted
    public int deleteDone(int diaryIdx) {
        String query = "UPDATE Done SET status = 'deleted' WHERE diaryIdx = ?";
        return this.jdbcTemplate.update(query, diaryIdx);
    }

    // =================================== 일기 조회 ===================================

//    // Diary
//    public GetDiaryRes getDiary(int diaryIdx) {
//        String query = "SELECT * FROM Diary WHERE diaryIdx = ? AND status = 'active'";
//        return this.jdbcTemplate.queryForObject(query,
//                (rs, rowNum) -> new GetDiaryRes(
//                        rs.getInt("diaryIdx"),
//                        rs.getInt("emotionIdx"),
//                        rs.getString("diaryDate"),
//                        rs.getString("content")
//                ), diaryIdx);
//    }
//
//    // Done
//    public List<Done> getDoneList(int diaryIdx) {
//        String query = "SELECT * FROM Done WHERE diaryIdx = ? AND status = 'active'";
//        return this.jdbcTemplate.query(query,
//                (rs, rowNum) -> new Done(
//                        rs.getInt("doneIdx"),
//                        rs.getString("content")
//                ), diaryIdx);
//    }
//
//    // Diary.isPublic 반환
//    public int getIsPublic(int diaryIdx) {
//        String query = "SELECT isPublic FROM Diary WHERE diaryIdx = ?";
//        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
//    }
//
//    // DiarySendList.isChecked = 1로 변환
//    public void modifyIsChecked(int userIdx, int diaryIdx) {
//        String query = "UPDATE DiarySendList SET isChecked = 1 WHERE receiverIdx = ? AND diaryIdx = ?";
//        this.jdbcTemplate.update(query, userIdx, diaryIdx);
//    }

    // =================================== 일기 발송 ===================================

    // 당일 발송해야 하는 모든 diaryIdx 반환
    public List<Integer> getDiaryIdxList(String date) {
        String query = "SELECT diaryIdx FROM Diary WHERE diaryDate = ? AND isPublic = 1 AND status = 'active'";
        return this.jdbcTemplate.queryForList(query, int.class, date);
    }

    // 수신 동의한 모든 userIdx 반환 (User.recOthers = 1)
//    public List<Integer> getUserIdxList_total() {
//        String query = "SELECT userIdx FROM User WHERE recOthers = 1 AND status = 'active'";
//        return this.jdbcTemplate.queryForList(query, int.class);
//    }

    // 일기 발송 가능한 회원 목록 반환 (User.recOthers = 1)
    public List<User> getUserList_total() {
        String query = "SELECT userIdx FROM User WHERE recOthers = 1 AND status = 'active'";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new User(
                        rs.getInt("userIdx")
                ));
    }

    // 회원마다 가장 최근에 받은 일기의 발신인(userIdx) 반환
    public int getUserIdx_recentReceived(int userIdx) {
        String checkQuery = "SELECT COUNT(userIdx)" +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "WHERE receiverIdx = ? " +
                "  AND DiarySendList.status = 'active'";

        if (this.jdbcTemplate.queryForObject(checkQuery, int.class, userIdx) == 0) { // 수신한 일기가 없는 경우
            return 0;

        } else {
            String query = "SELECT userIdx " +
                    "FROM DiarySendList " +
                    "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                    "WHERE receiverIdx = ? " +
                    "  AND DiarySendList.status = 'active' " +
                    "ORDER BY DiarySendList.createdAt DESC " + // createdAt 기준 내림차순 정렬
                    "LIMIT 1"; // 상위 첫번째 값

            return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
        }
    }

    // 비슷한 나이대 수신 동의한 회원 수 반환 (recSimilarAge = 1)
    public int getUserIdxNum_similarAge() {
        String query = "SELECT COUNT(*) FROM User WHERE recSimilarAge = 1 AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class);
    }

    // 발신인 userIdx 반환
    public int getSenderUserIdx(int diaryIdx) {
        String query = "SELECT Diary.userIdx FROM Diary WHERE diaryIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 발신인 nickName 반환
    public String getSenderNickName(int diaryIdx) {
        String query = "SELECT nickName FROM User " +
                "INNER JOIN Diary ON User.userIdx = Diary.userIdx " +
                "WHERE diaryIdx = ?";

        return this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
    }

    // 발신인 생년 반환 (User.birth)
    public int getSenderBirth(int diaryIdx) {
        String query = "SELECT birth FROM User " +
                "INNER JOIN Diary ON User.userIdx = Diary.userIdx " +
                "WHERE diaryIdx = ?";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 발송 가능한 & 비슷한 나이대를 갖는(senderBirth -5 ~ +5) & 일기를 같은 사람한테서 연속으로 2번 받지 않는 모든 userIdx 반환
    public List<Integer> getUserIdxList_similarAge(int userIdx, int senderBirth) {
        String query = "SELECT userIdx " +
                "FROM User " +
                "WHERE userIdx != ? " +
                "  AND userIdx != ALL (SELECT receiverIdx " + // 일기 작성자가 전에 작성한 일기를 가장 최근으로 발송받은 사람들은 제외
                "                      FROM DiarySendList " +
                "                               INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                      WHERE DiarySendList.diaryIdx = " +
                "                            (SELECT Diary.diaryIdx " +
                "                             FROM Diary " +
                "                             WHERE Diary.userIdx = ? " +
                "                             ORDER BY Diary.createdAt DESC " +
                "                             LIMIT 1) " +
                "                      ORDER BY Diary.createdAt DESC) " +
                "  AND recOthers = 1 " +
                "  AND recSimilarAge = 1 " +
                "  AND (birth >= ? OR birth <= ?) " +
                "  AND status = 'active'";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, userIdx, senderBirth - Constant.SIMILAR_AGE_STANDARD, senderBirth + Constant.SIMILAR_AGE_STANDARD);
    }

    // 일기 발송 (INSERT DiarySendList)
    public void setDiarySendList(int diaryIdx, int receiverIdx) {
        String query = "INSERT INTO DiarySendList(diaryIdx, receiverIdx) VALUES(?,?)";
        this.jdbcTemplate.update(query, diaryIdx, receiverIdx);
    }

    // 일기 발송 리스트 반환
    public List<Integer> getReceiverIdxList(int diaryIdx, String diaryDate) {
        String query = "SELECT receiverIdx " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "WHERE Diary.diaryIdx = ? " +
                "AND diaryDate = ? " +
                "AND DiarySendList.status = 'active' " +
                "ORDER BY receiverIdx";

        return this.jdbcTemplate.queryForList(query, int.class, diaryIdx, diaryDate);
    }

    // ================================================================================

    // 당일 발송되는 일기의 Diary.isSend = 1로 변경
    public void modifyIsSend() {
        String yesterday = LocalDate.now().minusDays(1).toString().replaceAll("-", "."); // 어제 날짜 (yyyy.MM.dd)

        String query = "UPDATE Diary SET isSend = 1 " +
                "WHERE diaryDate = ? AND isPublic = 1 AND status = 'active'";

        this.jdbcTemplate.update(query, yesterday);
    }

    // 수신인 User.fcmToken 반환
    public String getFcmToken(int userIdx) {
        String query = "SELECT fcmToken FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

    public ArrayList<Integer> pushUserList(List<Integer> receiverIdxList) {
        String query = "SELECT pushAlarm FROM User WHERE userIdx = ?";
        ArrayList<Integer> pushUsers = new ArrayList<>();
        for (int idx : receiverIdxList) {
            if (jdbcTemplate.queryForObject(query, int.class, idx) == 1) // 푸시 알람을 수신하는 유저만
                pushUsers.add(idx);
        }
        return pushUsers;
    }

}
