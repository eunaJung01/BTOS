package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.letter.model.PostLetterReq;

import com.umc.btos.src.plant.PlantService;
import com.umc.btos.src.reply.model.PostReplyReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.umc.btos.src.report.model.*;

import javax.sql.DataSource;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;
import static com.umc.btos.config.Constant.*;


@Repository
public class ReportDao {

    private JdbcTemplate jdbcTemplate;


    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);

    }

    // 편지의 경우 : 작성한 userIdx반환
    public int getUserIdx(PostReportReq postReportReq) {
        String getType = postReportReq.getReportType();
        if (getType.equals("diary")) { // 일기일 경우
            String selectQuery_diary = "SELECT Diary.userIdx FROM Diary WHERE diaryIdx=?";
            int param = postReportReq.getIdx();
            return this.jdbcTemplate.queryForObject(selectQuery_diary, int.class, param);
        } else if (getType.equals("letter")) { // 편지이 경우
            String selectQuery_letter = "SELECT Letter.userIdx FROM Letter WHERE letterIdx= ?";
            int param = postReportReq.getIdx();
            return this.jdbcTemplate.queryForObject(selectQuery_letter, int.class, param);
        } else if (getType.equals("reply")) { // 답장일 경우
            String selectQuery_reply = "SELECT Reply.replierIdx FROM Reply WHERE replyIdx=?";
            int param = postReportReq.getIdx();
            return this.jdbcTemplate.queryForObject(selectQuery_reply, int.class, param);
        }
        return 1;
    }


    public int createReport(PostReportReq postReportReq) {
        String createReportQuery = "insert into Report (reportType,reason,idx,content) VALUES (?,?,?,?)"; // 실행될 동적 쿼리문
        Object[] createReportParams = new Object[]{postReportReq.getReportType(), postReportReq.getReason(), postReportReq.getIdx(), postReportReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createReportQuery, createReportParams);

        // 즉 DB의 Report Table에 (reportType,reason,idx,content)값을 가지는 유저 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }
}
