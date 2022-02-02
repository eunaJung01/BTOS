package com.umc.btos.src.diary;

import com.umc.btos.src.diary.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
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

}
