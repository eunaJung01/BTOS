package com.umc.btos.src.archive;

import com.umc.btos.config.Constant;
import com.umc.btos.src.archive.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ArchiveDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDateSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 달력 조회 (diaryDate(일기의 해당 날짜) 기준 오름차순 정렬)
    public List<GetCalendarRes> getCalendarList(int userIdx, String date) {
        String startDate = date + ".01";
        String endDate = date + ".31";

        String query = "SELECT diaryDate FROM Diary WHERE userIdx = ? AND DATE_FORMAT(diaryDate, '%Y.%m.%d') >= DATE_FORMAT(?, '%Y.%m.%d') AND DATE_FORMAT(diaryDate, '%Y.%m.%d') <= DATE_FORMAT(?, '%Y.%m.%d') AND status = 'active' ORDER BY diaryDate ASC";

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
        String query = "SELECT isPremium FROM User WHERE userIdx = ?";
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

        String query = "SELECT * FROM Diary WHERE userIdx = ? AND DATE_FORMAT(diaryDate, '%Y.%m.%d') >= DATE_FORMAT(?, '%Y.%m.%d') AND DATE_FORMAT(diaryDate, '%Y.%m.%d') <= DATE_FORMAT(?, '%Y.%m.%d') AND status = 'active' ORDER BY diaryDate DESC LIMIT ?, ?";
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
        String query = "SELECT COUNT(*) FROM Diary WHERE userIdx = ? AND DATE_FORMAT(diaryDate, '%Y.%m.%d') >= DATE_FORMAT(?, '%Y.%m.%d') AND DATE_FORMAT(diaryDate, '%Y.%m.%d') <= DATE_FORMAT(?, '%Y.%m.%d') AND status = 'active'";
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
    public List<Done> getDoneList(int diaryIdx) {
        String query = "SELECT * FROM Done WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Done(
                        rs.getInt("doneIdx"),
                        rs.getString("content")
                ), diaryIdx);
    }

}
