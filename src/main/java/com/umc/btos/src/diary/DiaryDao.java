package com.umc.btos.src.diary;

import com.umc.btos.src.diary.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
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

    // =================================== 일기 저장 ===================================

    // 일기 저장 -> diaryIdx 반환
    public int saveDiary(PostDiaryReq postDiaryReq) {
        String query = "INSERT INTO Diary(userIdx, emotionIdx, diaryDate, isPublic, content) VALUES(?,?,?,?,?)";
        Object[] diary = new Object[]{
                postDiaryReq.getUserIdx(), postDiaryReq.getEmotionIdx(), postDiaryReq.getDiaryDate(), postDiaryReq.getIsPublic(), postDiaryReq.getDiaryContent()
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
        Object[] params = new Object[]{putDiaryReq.getEmotionIdx(), putDiaryReq.getDiaryDate(), putDiaryReq.getIsPublic(), putDiaryReq.getDiaryContent(), putDiaryReq.getDiaryIdx()};
        return this.jdbcTemplate.update(query, params);
    }

    // 해당 일기의 모든 doneIdx를 List 형태로 반환
    public List getDoneIdxList(PutDiaryReq putDiaryReq) {
        String query = "SELECT doneIdx FROM Done WHERE diaryIdx = ?";
        return this.jdbcTemplate.queryForList(query, int.class, putDiaryReq.getDiaryIdx());
    }

    // done list 수정
    public int modifyDoneList(PutDiaryReq putDiaryReq, List doneIdxList) {
        String query = "UPDATE Done SET content = ? WHERE doneIdx = ?";
        for (int i = 0; i < doneIdxList.size(); i++) {
            int result = this.jdbcTemplate.update(query, putDiaryReq.getDoneList().get(i), doneIdxList.get(i));

            if (result == 0) { // MODIFY_FAIL_DONELIST(일기 수정 실패 - done list) 에러 반환
                return 0;
            }
        }
        return 1;
    }

    // =================================== 일기 삭제 ===================================

    // 일기 삭제 - Diary.status : active -> deleted
    public int deleteDiary(int diaryIdx) {
        String query = "UPDATE Diary SET status = ? WHERE diaryIdx = ?";
        Object[] params = new Object[]{"deleted", diaryIdx};
        return this.jdbcTemplate.update(query, params);
    }

    // done list 삭제 - Done.status : active -> deleted
    public int deleteDone(int diaryIdx) {
        String query = "UPDATE Done SET status = ? WHERE diaryIdx = ?";
        Object[] params = new Object[]{"deleted", diaryIdx};
        return this.jdbcTemplate.update(query, params);
    }

    // =================================== 일기 조회 ===================================

    // Diary
    public GetDiaryRes getDiary(int diaryIdx) {
        String query = "SELECT * FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetDiaryRes(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("diaryDate"),
                        rs.getInt("isPublic"),
                        rs.getString("content")
                ), diaryIdx);
    }

    // Done
    public List<GetDoneRes> getDoneList(int diaryIdx) {
        String query = "SELECT * FROM Done WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetDoneRes(
                        rs.getInt("doneIdx"),
                        rs.getString("content")
                ), diaryIdx);
    }

    // =================================== 일기 발송 ===================================

    // 당일 발송해야 하는 모든 diaryIdx 반환
    public List<Integer> getDiaryIdxList(String yesterday, String today) {
        String time = "19:00:00";
        yesterday = yesterday + " " + time;
        today = today + " " + time;

        String query = "select diaryIdx " +
                "from Diary " +
                "where createdAt > ? " +
                "  and createdAt <= ? " +
                "  and isPublic = 1 " +
                "  and status = 'active'";
        return this.jdbcTemplate.queryForList(query, int.class, yesterday, today);
    }

    // 수신 동의한 모든 userIdx 반환 (User.recOthers = 1)
    public List<Integer> getUserIdxList_total() {
        String query = "SELECT userIdx FROM User WHERE recOthers = 1 AND status = 'active'";
        return this.jdbcTemplate.queryForList(query, int.class);
    }

    // 비슷한 나이대 수신 동의한 회원 수 반환 (recSimilarAge = 1)
    public int getUserIdxNum_similarAge() {
        String query = "SELECT COUNT(*) FROM User WHERE recSimilarAge = 1 AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class);
    }

    // 발신인 userIdx 반환
    public int getSenderUserIdx(int diaryIdx) {
        String query = "SELECT User.userIdx FROM User " +
                "INNER JOIN Diary ON User.userIdx = Diary.userIdx " +
                "WHERE diaryIdx = ?";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 발신인 생년 반환 (User.birth)
    public int getSenderBirth(int diaryIdx) {
        String query = "SELECT birth FROM User " +
                "INNER JOIN Diary ON User.userIdx = Diary.userIdx " +
                "WHERE diaryIdx = ?";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 발송 가능한 & 비슷한 나이대를 갖는(senderBirth -5 ~ +5) 모든 userIdx 반환
    public List<Integer> getUserIdxList_similarAge(int userIdx, int senderBirth) {
        String query = "SELECT userIdx FROM User " +
                "WHERE userIdx != ? " + // 발신인 userIdx 제외
                "AND recOthers = 1 AND recSimilarAge = 1 " +
                "AND (birth >= ? OR birth <= ?)" +
                "AND status = 'active'";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderBirth - 5, senderBirth + 5);
    }

    // 일기 발송 (DiarySendList)
    public void setDiarySendList(int diaryIdx, int receiverIdx) {
        String query = "INSERT INTO DiarySendList(diaryIdx, receiverIdx) VALUES(?,?)";

        Object[] diarySendList = new Object[]{
                diaryIdx, receiverIdx
        };
        this.jdbcTemplate.update(query, diarySendList);
    }

    // 일기 발송 리스트 반환
    public List<Integer> getReceiverIdxList(int diaryIdx, String date) {
        String query  = "SELECT receiverIdx FROM DiarySendList " +
                "WHERE diaryIdx = ? AND left(createdAt, 10) = ? AND status = 'active' " +
                "ORDER BY receiverIdx";

        return this.jdbcTemplate.queryForList(query, int.class, diaryIdx, date);
    }

    // ================================================================================

    // TODO : 매일 19:00:00에 당일 발송되는 일기의 Diary.isSend = 1로 변경
    @Scheduled(cron = "00 00 19 * * *")
//    @Scheduled(cron = "30 23 19 * * *") // test
    public void modifyIsSend() {
        LocalDate now = LocalDate.now(); // 오늘 날짜 (yyyy-MM-dd)

        String time = "19:00:00";
        String yesterday = now.minusDays(1) + " " + time;
        String today = now + " " + time;

        String query = "update Diary " +
                "set isSend = 1 " +
                "where createdAt > ? " +
                "  and createdAt <= ? " +
                "  and isPublic = 1 " +
                "  and status = 'active'";

        this.jdbcTemplate.update(query, yesterday, today);
    }

}
