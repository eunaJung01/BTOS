package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.letter.model.PostLetterReq;

import com.umc.btos.src.reply.model.PostReplyReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.umc.btos.src.report.model.*;

import javax.sql.DataSource;

import static com.umc.btos.config.Constant.*;

@SpringBootApplication // component로 등록된 것은 @SpringBootApplication 어노테이션을 통해 componentscan을 한다고 한다.
@Repository
public class ReportDao {
/**
    private JdbcTemplate jdbcTemplate;
    private PlantService plantService;
    private PostReplyReq postReplyReq;
    private PostLetterReq postLetterReq;


    //@Autowired
    public void setDataSource(DataSource dataSource,PlantService plantService,PostReplyReq postReplyReq, PostLetterReq postLetterReq) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.plantService  = plantService;
        this.postReplyReq = postReplyReq;
        this.postLetterReq = postLetterReq;
    }
    // 답장의 경우 : 작성한 userIdx반환
    public int replyUserIdx(int Idx, PostReplyReq postReplyReq) {
        String query = "SELECT postReplyReq.replierIdx FROM Reply WHERE postReplyReq.replyIdx = Idx";
        return this.jdbcTemplate.queryForObject(query, int.class );
    }
    // 편지의 경우 : 작성한 userIdx반환
    public int letterUserIdx(int Idx, PostLetterReq postLetterReq) {
        String query = "SELECT postLetterReq.userIdx FROM Letter WHERE postLetterReq.letterIdx = Idx";
        return this.jdbcTemplate.queryForObject(query, int.class);
    }
    public int createReport(PostReportReq postReportReq) throws BaseException {
        String createReportQuery = "insert into Report (reportType,reason,idx,content) VALUES (?,?,?,?)"; // 실행될 동적 쿼리문
        Object[] createReportParams = new Object[]{postReportReq.getReportType(),postReportReq.getReason(),postReportReq.getIdx(),postReportReq.getContent()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createReportQuery, createReportParams);
        if (postReportReq.getReason() == "sex" ||postReportReq.getReason() == "hate" ) { // 성과 혐오
            PatchUpDownScoreReq patchUpDownScoreReq1 = new PatchUpDownScoreReq(PLANT_LEVELDOWN_REPORT_SEX_HATE); // addscore 는 3점
            // 화분 점수 감소
            if (postReportReq.getReportType()=="diary") { // 일기일경우
                plantService.upScore(replyUserIdx(postReportReq.getIdx(),postReplyReq),patchUpDownScoreReq1);
            }
            if (postReportReq.getReportType()=="reply") { // 답장일경우
                plantService.upScore(replyUserIdx(postReportReq.getIdx(),postReplyReq),patchUpDownScoreReq1);
            }
            if (postReportReq.getReportType()=="letter") { // 편지일경우
                plantService.upScore(letterUserIdx(postReportReq.getIdx(),postLetterReq),patchUpDownScoreReq1);
            }
        }
        else if (postReportReq.getReason() == "spam" || postReportReq.getReason() == "dislike"){ // 스팸과 마음에 안듦
            // 화분 점수 감소
            PatchUpDownScoreReq patchUpDownScoreReq2 = new PatchUpDownScoreReq(PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE); // addscore 는 3점
            // 화분 점수 감소
            if (postReportReq.getReportType()=="diary") { // 일기일경우
                plantService.upScore(postReportReq.getIdx(),patchUpDownScoreReq2);
            }
            if (postReportReq.getReportType()=="reply") { // 답장일경우
                plantService.upScore(replyUserIdx(postReportReq.getIdx(),postReplyReq),patchUpDownScoreReq2);
            }
            if (postReportReq.getReportType()=="letter") { // 편지일경우
                plantService.upScore(letterUserIdx(postReportReq.getIdx(),postLetterReq),patchUpDownScoreReq2);
            }
        }

        // 즉 DB의 Report Table에 (reportType,reason,idx,content)값을 가지는 신고 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 reportIdx번호를 반환한다.
    }
     */
}
