package com.umc.btos.src.archive;

import com.umc.btos.config.Constant;
import com.umc.btos.src.archive.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ArchiveDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDateSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // ================================================== validation ==================================================

    // 존재하는 회원인지 확인
    public int checkUserIdx(int userIdx) {
        String query = "SELECT EXISTS (SELECT userIdx FROM User WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 존재하는 일기인지 확인
    public int checkDiaryIdx(int diaryIdx) {
        String query = "SELECT EXISTS (SELECT diaryIdx FROM Diary WHERE diaryIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // ================================================== 달력 조회 ===================================================

    // 달력 조회 (diaryDate(일기의 해당 날짜) 기준 오름차순 정렬)
    public List<GetCalendarRes> getCalendarList(int userIdx, String date) {
        String startDate = date + ".01";

        int year = Integer.parseInt(date.substring(0, 4)); // date[0] ~ date[3]
        int month = Integer.parseInt(date.substring(5, 7)); // date[5] ~ date[6]
        LocalDate initial = LocalDate.of(year, month, 1); // yyyy.MM.01
        LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth()); // 해당 월의 마지막 날 가져오기

        String query = "SELECT diaryIdx, diaryDate " +
                "FROM Diary " +
                "WHERE userIdx = ? " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') >= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') <= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND status = 'active' " +
                "ORDER BY diaryDate ASC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetCalendarRes(
                        rs.getInt("diaryIdx"),
                        rs.getString("diaryDate")
                ), userIdx, startDate, endDate);
    }

    // -------------------------------------------------------------------------------------------

    // Diary.emotionIdx 반환
    public int getEmotionIdx(int userIdx, String diaryDate) {
        String query = "SELECT emotionIdx FROM Diary WHERE userIdx = ? AND diaryDate = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, diaryDate);
    }

    // isPremium 반환
    public String isPremium(int userIdx) {
        String query = "SELECT isPremium FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

    // isPublic 반환
    public int getIsPublic(int diaryIdx) {
        String query = "SELECT isPublic FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // --------------------------------------- doneListNum ---------------------------------------

    // 달력 조회
    public int setDoneListNum(int userIdx, String diaryDate) {
        String query = "SELECT COUNT(*) FROM Done " +
                "WHERE diaryIdx = " +
                "(SELECT diaryIdx FROM Diary WHERE userIdx = ? AND diaryDate = ? AND status = 'active') " +
                "AND status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, diaryDate);
    }

    // 일기 리스트 조회
    public int setDoneListNum(int diaryIdx) {
        String query = "SELECT COUNT(*) FROM Done " +
                "WHERE diaryIdx = " +
                "(SELECT diaryIdx FROM Diary WHERE diaryIdx = ? AND status = 'active') " +
                "AND status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // ====================================== 일기 리스트 조회 ======================================

    // --------------------------------------- List<Diary> ---------------------------------------

    // 1. 전체 조회 (diaryDate 기준 내림차순 정렬)
    public List<Diary> getDiaryList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.DIARYLIST_DATA_NUM;
        int endData = pageNum * Constant.DIARYLIST_DATA_NUM;

        String query = "SELECT * FROM Diary " +
                "WHERE userIdx = ? AND status = 'active' " +
                "ORDER BY diaryDate DESC LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Diary(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("diaryDate"),
                        rs.getString("content")
                ), userIdx, startData, endData);
    }

    // 3. 기간 설정 조회 / 4. 문자열 검색 & 기간 설정 조회 (diaryDate 기준 내림차순 정렬)
    public List<Diary> getDiaryListByDate(int userIdx, String startDate, String endDate, int pageNum) {
        int startData = (pageNum - 1) * Constant.DIARYLIST_DATA_NUM;
        int endData = pageNum * Constant.DIARYLIST_DATA_NUM;

        String query = "SELECT * FROM Diary " +
                "WHERE userIdx = ? " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') >= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') <= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND status = 'active' " +
                "ORDER BY diaryDate DESC LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Diary(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("diaryDate"),
                        rs.getString("content")
                ), userIdx, startDate, endDate, startData, endData);
    }

    // --------------------------------------- COUNT(List<Diary>) ---------------------------------------

    // 1. 전체 조회
    public int getDiaryList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM Diary " +
                "WHERE userIdx = ? AND status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 3. 기간 설정 조회 / 4. 문자열 검색 & 기간 설정 조회
    public int getDiaryListByDate_dataNum(int userIdx, String startDate, String endDate) {
        String query = "SELECT COUNT(*) FROM Diary " +
                "WHERE userIdx = ? " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') >= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') <= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, startDate, endDate);
    }

    // --------------------------------------- diaryIdxList ---------------------------------------

    // 2. 문자열 검색
    // 특정 회원의 모든 일기 diaryIdx (diaryDate 기준 내림차순 정렬)
    public List<Integer> getDiaryIdxList(int userIdx) {
        String query = "SELECT diaryIdx FROM Diary " +
                "WHERE userIdx = ? AND status = 'active' " +
                "ORDER BY diaryDate DESC";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx);
    }

    // --------------------------------------- Diary ---------------------------------------

    // 일기 조회
    public Diary getDiary_diaryList(int diaryIdx) {
        String query = "SELECT * FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new Diary(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("diaryDate"),
                        rs.getString("content")
                ), diaryIdx);
    }

    // Diary.content
    public String getDiaryContent(int diaryIdx) {
        String query = "SELECT content FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
    }

    // --------------------------------------- monthList ---------------------------------------

    // 1. 전체 조회
    public List<String> getMonthList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.DIARYLIST_DATA_NUM;
        int endData = pageNum * Constant.DIARYLIST_DATA_NUM;

        String query = "SELECT DISTINCT parsed_diaryDate " +
                "FROM (SELECT left(diaryDate, 7) AS parsed_diaryDate, diaryDate " +
                "FROM Diary " +
                "WHERE userIdx = ? " +
                "LIMIT ?, ?) parsed_diaryDate " +
                "ORDER BY parsed_diaryDate DESC";

        return this.jdbcTemplate.queryForList(query, String.class, userIdx, startData, endData);
    }

    // 2. 문자열 검색 / 4. 문자열 검색 & 기간 설정 조회 (search != null)
    public List<String> getMonthList(int userIdx, List<Integer> idxList) {
        String WHERE_diaryIdx_query = "";
        for (Integer diaryIdx : idxList) {
            WHERE_diaryIdx_query += " OR diaryIdx = " + diaryIdx;
        }

        String query = "SELECT DISTINCT parsed_diaryDate " +
                "FROM (SELECT left(diaryDate, 7) AS parsed_diaryDate, diaryDate " +
                "FROM Diary " +
                "      WHERE userIdx = ?" + WHERE_diaryIdx_query +
                ") parsed_diaryDate " +
                "ORDER BY parsed_diaryDate DESC";

        return this.jdbcTemplate.queryForList(query, String.class, userIdx);
    }

    // 3. 기간 설정 조회 (paging)
    public List<String> getMonthList(int userIdx, String startDate, String endDate, int pageNum) {
        int startData = (pageNum - 1) * Constant.DIARYLIST_DATA_NUM;
        int endData = pageNum * Constant.DIARYLIST_DATA_NUM;

        String query = "SELECT DISTINCT parsed_diaryDate " +
                "FROM (SELECT left(diaryDate, 7) AS parsed_diaryDate, diaryDate FROM Diary " +
                "WHERE userIdx = ? " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') >= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND DATE_FORMAT(diaryDate, '%Y.%m.%d') <= DATE_FORMAT(?, '%Y.%m.%d') " +
                "AND status = 'active' " +
                "LIMIT ?, ?) parsed_diaryDate " +
                "ORDER BY parsed_diaryDate DESC";

        return this.jdbcTemplate.queryForList(query, String.class, userIdx, startDate, endDate, startData, endData);
    }

    // =================================== 일기 조회 ===================================

    // Diary
    public Diary getDiary(int diaryIdx) {
        String query = "SELECT * FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new Diary(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("diaryDate"),
                        rs.getString("content")
                ), diaryIdx);
    }

    // done list 개수 반환
    public boolean hasDoneList(int diaryIdx) {
        String query = "SELECT EXISTS (SELECT COUNT(*) FROM Done WHERE diaryIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, boolean.class, diaryIdx);
    }

    // Done
    public List<String> getDoneList(int diaryIdx) {
        String query = "SELECT content FROM Done WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForList(query, String.class, diaryIdx);
    }

}
