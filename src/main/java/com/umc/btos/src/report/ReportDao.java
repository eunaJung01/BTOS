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

    public int createReport(PostReportReq postReportReq) {
        String createReportQuery = "insert into Report (reportType,reason,idx,content) VALUES (?,?,?,?)"; // 실행될 동적 쿼리문
        Object[] createReportParams = new Object[]{postReportReq.getReportType(),postReportReq.getReason(),postReportReq.getIdx(),postReportReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createReportQuery, createReportParams);

        // 즉 DB의 Report Table에 (reportType,reason,idx,content)값을 가지는 신고 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 reportIdx번호를 반환한다.
    }
}
