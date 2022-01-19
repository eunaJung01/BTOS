package com.umc.btos.src.diary;

import com.umc.btos.src.diary.model.GetCalendarRes;
import com.umc.btos.src.diary.model.PostDiaryReq;
import com.umc.btos.src.diary.model.PutDiaryReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DiaryDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDateSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 해당 날짜에 일기 작성 여부 확인
    public int checkDiary(int userIdx, String date) {
        String query = "SELECT EXISTS (SELECT diaryDate FROM Diary WHERE userIdx = ? AND diaryDate = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, date);
    }

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

    // Done List 저장 -> doneIdxList 반환
    public List saveDoneList(int diaryIdx, List doneList) {
        List doneListIdx = new ArrayList(); // doneIdx를 담는 배열

        String query = "INSERT INTO Done(diaryIdx, content) VALUES(?,?)";
        for (Object doneContent : doneList) {
            Object[] done = new Object[]{
                    diaryIdx, doneContent
            };
            this.jdbcTemplate.update(query, done); // Done Table에 순차적으로 저장

            String get_doneIdx_query = "SELECT last_insert_id()";
            doneListIdx.add(this.jdbcTemplate.queryForObject(get_doneIdx_query, int.class)); // doneIdxList에 해당 doneIdx 저장
        }

        return doneListIdx;
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

    // 캘린더 조회 (일기 해당 날짜(diaryDate) 기준 오름차순 정렬)
    public List<GetCalendarRes> getCalendarList(int userIdx, String date) {
        String startDate = date + "-01";
        String endDate = date + "-31";

        String query = "SELECT diaryDate FROM Diary WHERE userIdx = ? AND DATE_FORMAT(diaryDate, '%Y-%m-%d') >= DATE_FORMAT(?, '%Y-%m-%d') AND DATE_FORMAT(diaryDate, '%Y-%m-%d') <= DATE_FORMAT(?, '%Y-%m-%d') ORDER BY diaryDate ASC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetCalendarRes((
                        rs.getString("diaryDate")
                )),
                userIdx, startDate, endDate);
    }

    // 일기 별 done list 개수 반환 : COUNT(Done.diaryIdx)
    public int setDoneListNum(int userIdx, String diaryDate) {
        String query = "SELECT COUNT(*) FROM Done WHERE diaryIdx = (SELECT diaryIdx FROM Diary WHERE userIdx = ? AND diaryDate = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, diaryDate);
    }

    // 일기 별 감정 이모티콘 반환 : Diary.emotionIdx
    public int setEmotion(int userIdx, String diaryDate) {
        String query = "SELECT emotionIdx FROM Diary WHERE userIdx = ? AND diaryDate = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, diaryDate);
    }

    // 프리미엄 가입자인지 확인
    public String isPremium(int userIdx) {
        String query = "SELECT isPremium FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

}
