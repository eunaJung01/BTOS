package com.umc.btos.src.alarm;

import com.umc.btos.src.alarm.model.GetAlarmListRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class AlarmDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDateSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 존재하는 회원인지 확인
    public int checkUserIdx(int userIdx) {
        String query = "SELECT EXISTS (SELECT userIdx FROM User WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // ====================================== 알림 목록 조회 ======================================

    // 알림 목록 창에 띄워줄 알림이 존재하는지 확인 (Alarm.status = active)
    public int checkAlarmList(int userIdx) {
        String query = "SELECT EXISTS (SELECT * FROM Alarm WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 알림 목록 반환
    public List<GetAlarmListRes> getAlarmList(int userIdx) {
        String query = "SELECT alarmIdx, content, createdAt " +
                "FROM Alarm " +
                "WHERE userIdx = ? AND status = 'active' " +
                "ORDER BY createdAt DESC"; // createdAt 기준 내림차순 정렬

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetAlarmListRes(
                        rs.getInt("alarmIdx"),
                        rs.getString("content"),
                        rs.getString("createdAt")
                ), userIdx);
    }

    // ====================================== 알림 조회 ======================================

    // Alarm.type 반환
    public String getAlarmType(int alarmIdx) {
        String query = "SELECT type FROM Alarm WHERE alarmIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, alarmIdx);
    }

    // Alarm.typeIdx 반환
    public int getAlarmTypeIdx(int alarmIdx) {
        String query = "SELECT typeIdx FROM Alarm WHERE alarmIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, alarmIdx);
    }

    // Report.reportType 반환
    public String getReportType(int alarmIdx, int typeIdx) {
        String query = "SELECT Report.reportType " +
                "FROM Alarm " +
                "INNER JOIN Report ON Alarm.typeIdx = Report.reportIdx " +
                "WHERE Alarm.alarmIdx = ? AND Alarm.typeIdx = ?";

        return this.jdbcTemplate.queryForObject(query, String.class, alarmIdx, typeIdx);
    }

    // Alarm.status = active -> checked
    public int modifyStatus(int alarmIdx) {
        String query = "UPDATE Alarm SET status = 'checked' WHERE alarmIdx = ?";
        return this.jdbcTemplate.update(query, alarmIdx);
    }

}
