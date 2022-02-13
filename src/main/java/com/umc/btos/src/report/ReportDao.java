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

    // 신고 당한 UserIdx 반환
    public int getUserIdx(PostReportReq postReportReq) {
        // 신고의 Type 반환  // diary:일기, letter : 편지, reply : 답장
        String getType = postReportReq.getReportType();

        // TYPE : 일기
        if (getType.equals("diary")) {
            String selectQuery_diary = "SELECT Diary.userIdx FROM Diary WHERE diaryIdx=?";
            int param = postReportReq.getIdx();
            return this.jdbcTemplate.queryForObject(selectQuery_diary, int.class, param);
        }
        // TYPE : 편지
        else if (getType.equals("letter")) {
            String selectQuery_letter = "SELECT Letter.userIdx FROM Letter WHERE letterIdx= ?";
            int param = postReportReq.getIdx();
            return this.jdbcTemplate.queryForObject(selectQuery_letter, int.class, param);
        }
        // TYPE : 답장
        else if (getType.equals("reply")) {
            String selectQuery_reply = "SELECT Reply.replierIdx FROM Reply WHERE replyIdx=?";
            int param = postReportReq.getIdx();
            return this.jdbcTemplate.queryForObject(selectQuery_reply, int.class, param);
        }
        return 1;
    }

    // 신고 생성 // Report 테이블에 값 추가
    public int createReport(PostReportReq postReportReq) {
        // DB의 Report Table에 (reportType,reason,idx,content)값을 가지는 신고 데이터를 생성한다.
        String createReportQuery = "insert into Report (reportType,reason,idx,content) VALUES (?,?,?,?)";
        Object[] createReportParams = new Object[]{postReportReq.getReportType(), postReportReq.getReason(), postReportReq.getIdx(), postReportReq.getContent()};
        this.jdbcTemplate.update(createReportQuery, createReportParams);

        // 가장 마지막에 삽입된 reportIdx값을 가져온다.
        String lastInsertIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class);
    }
}
