package com.umc.btos.src.diary;

import com.umc.btos.config.Constant;
import com.umc.btos.src.diary.model.*;
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

    // 해당 날짜에 일기 작성 여부 확인 (1 : 작성함, 0 : 작성 안 함)
    public int checkDiaryDate(int userIdx, String date) {
        String query = "SELECT EXISTS (SELECT diaryDate FROM Diary WHERE userIdx = ? AND diaryDate = ? AND status = 'active')";
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
        String query = "UPDATE Diary SET emotionIdx = ?, diaryDate = ?, isPublic = ?, content = ? WHERE diaryIdx = ? AND status = 'active'";
        Object[] params = new Object[]{putDiaryReq.getEmotionIdx(), putDiaryReq.getDiaryDate(), putDiaryReq.getIsPublic(), putDiaryReq.getDiaryContent(), putDiaryReq.getDiaryIdx()};
        return this.jdbcTemplate.update(query, params);
    }

    // 해당 일기의 모든 doneIdx를 List 형태로 반환
    public List getDoneIdxList(PutDiaryReq putDiaryReq) {
        String query = "SELECT doneIdx FROM Done WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForList(query, int.class, putDiaryReq.getDiaryIdx());
    }

    // done list 수정
    public int modifyDoneList(PutDiaryReq putDiaryReq, List doneIdxList) {
        String query = "UPDATE Done SET content = ? WHERE doneIdx = ? AND status = 'active'";
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
        String query = "UPDATE Diary SET status = ? WHERE diaryIdx = ? AND status = 'active'";
        Object[] params = new Object[]{"deleted", diaryIdx};
        return this.jdbcTemplate.update(query, params);
    }

    // done list 삭제 - Done.status : active -> deleted
    public int deleteDone(int diaryIdx) {
        String query = "UPDATE Done SET status = ? WHERE diaryIdx = ? AND status = 'active'";
        Object[] params = new Object[]{"deleted", diaryIdx};
        return this.jdbcTemplate.update(query, params);
    }

    // 캘린더 조회 (diaryDate(일기의 해당 날짜) 기준 오름차순 정렬)
    public List<GetCalendarRes> getCalendarList(int userIdx, String date) {
        String startDate = date + "-01";
        String endDate = date + "-31";

        String query = "SELECT diaryDate FROM Diary WHERE userIdx = ? AND DATE_FORMAT(diaryDate, '%Y-%m-%d') >= DATE_FORMAT(?, '%Y-%m-%d') AND DATE_FORMAT(diaryDate, '%Y-%m-%d') <= DATE_FORMAT(?, '%Y-%m-%d') AND status = 'active' ORDER BY diaryDate ASC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetCalendarRes(
                        rs.getString("diaryDate")
                ), userIdx, startDate, endDate);
    }

    // 일기 별 done list 개수 반환 : COUNT(Done.diaryIdx)
    public int setDoneListNum(int userIdx, String diaryDate) {
        String query = "SELECT COUNT(*) FROM Done WHERE diaryIdx = (SELECT diaryIdx FROM Diary WHERE userIdx = ? AND diaryDate = ? AND status = 'active') AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, diaryDate);
    }

    // 일기 별 감정 이모티콘 식별자 반환 : Diary.emotionIdx
    public int setEmotion(int userIdx, String diaryDate) {
        String query = "SELECT emotionIdx FROM Diary WHERE userIdx = ? AND diaryDate = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, diaryDate);
    }

    // 프리미엄 가입자인지 확인 (1 : 프리미엄 O, 0 : 프리미엄 X)
    public String isPremium(int userIdx) {
        String query = "SELECT isPremium FROM User WHERE userIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

    // 일기 리스트 반환 - 전체 조회 (최신순 정렬 - diaryDate 기준 내림차순 정렬)
    public List<GetDiaryRes> getDiaryList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.DIARYLIST_DATA_NUM;
        int endData = pageNum * Constant.DIARYLIST_DATA_NUM;

        String query = "SELECT * FROM Diary WHERE userIdx = ? AND status = 'active' ORDER BY diaryDate DESC LIMIT ?, ?";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetDiaryRes(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("diaryDate"),
                        rs.getInt("isPublic"),
                        rs.getString("content")
                ), userIdx, startData, endData);
    }

    // 일기 리스트 전체 조회 시 data 개수 반환
    public int getDiaryList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM Diary WHERE userIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 일기 리스트 반환 - 날짜 기간으로 조회 (최신순 정렬 - diaryDate 기준 내림차순 정렬)
    public List<GetDiaryRes> getDiaryListByDate(int userIdx, String startDate, String endDate, int pageNum) {
        int startData = (pageNum - 1) * Constant.DIARYLIST_DATA_NUM;
        int endData = pageNum * Constant.DIARYLIST_DATA_NUM;

        String query = "SELECT * FROM Diary WHERE userIdx = ? AND DATE_FORMAT(diaryDate, '%Y-%m-%d') >= DATE_FORMAT(?, '%Y-%m-%d') AND DATE_FORMAT(diaryDate, '%Y-%m-%d') <= DATE_FORMAT(?, '%Y-%m-%d') AND status = 'active' ORDER BY diaryDate DESC LIMIT ?, ?";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetDiaryRes(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("diaryDate"),
                        rs.getInt("isPublic"),
                        rs.getString("content")
                ), userIdx, startDate, endDate, startData, endData);
    }

    // 일기 리스트 날짜 기간으로 조회 시 data 개수 반환
    public int getDiaryListByDate_dataNum(int userIdx, String startDate, String endDate) {
        String query = "SELECT COUNT(*) FROM Diary WHERE userIdx = ? AND DATE_FORMAT(diaryDate, '%Y-%m-%d') >= DATE_FORMAT(?, '%Y-%m-%d') AND DATE_FORMAT(diaryDate, '%Y-%m-%d') <= DATE_FORMAT(?, '%Y-%m-%d') AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, startDate, endDate);
    }

    // 특정 회원의 모든 일기 diaryIdx : List 형태로 반환 (최신순 정렬 - diaryDate 기준 내림차순 정렬)
    public List<Integer> getDiaryIdxList(int userIdx) {
        String query = "SELECT diaryIdx FROM Diary WHERE userIdx = ? AND status = 'active' ORDER BY diaryDate DESC";
        return this.jdbcTemplate.queryForList(query, int.class, userIdx);
    }

    // isPublic 반환
    public int getIsPublic(int diaryIdx) {
        String query = "SELECT isPublic FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // Diary.content 반환
    public String getDiaryContent(int diaryIdx) {
        String query = "SELECT content FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
    }

    // 일기 조회
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

    // done list 조회
    public List<GetDoneRes> getDoneList(int diaryIdx) {
        String query = "SELECT * FROM Done WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetDoneRes(
                        rs.getInt("doneIdx"),
                        rs.getString("content")
                ), diaryIdx);
    }

}
