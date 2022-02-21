package com.umc.btos.src.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.umc.btos.src.report.model.*;

import javax.sql.DataSource;

@Repository
public class ReportDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 신고 저장
    public int postReport(PostReportReq postReportReq) {
        String query = "INSERT INTO Report (type, typeIdx, reason, content) VALUES (?,?,?,?)";
        Object[] params = new Object[]{postReportReq.getType(), postReportReq.getTypeIdx(), postReportReq.getReason(), postReportReq.getContent()};
        this.jdbcTemplate.update(query, params);

        // replyIdx 반환
        String query_getReplyIdx = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(query_getReplyIdx, int.class);
    }

    // 신고를 당한 회원 식별자 반환
    public int getReportedUserIdx(String type, int typeIdx) {
        /*
         * type - typeIdx
         * diary - diaryIdx / letter - letterIdx / reply - replyIdx
         */
        String query;
        if (type.equals("diary")) {
            query = "SELECT Diary.userIdx FROM Diary WHERE diaryIdx = ?";
        }
        // TYPE : 편지
        else if (type.equals("letter")) {
            query = "SELECT Letter.userIdx FROM Letter WHERE letterIdx = ?";
        }
        else {
            query = "SELECT Reply.replierIdx FROM Reply WHERE replyIdx = ?";
        }

        return this.jdbcTemplate.queryForObject(query, int.class, typeIdx);
    }

}
