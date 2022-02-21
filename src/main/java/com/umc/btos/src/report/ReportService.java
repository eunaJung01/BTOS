package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.report.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class ReportService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReportDao reportDao;
    private final ReportProvider reportProvider;

    @Autowired
    public ReportService(ReportDao reportDao, ReportProvider reportProvider) {
        this.reportDao = reportDao;
        this.reportProvider = reportProvider;
    }

    /*
     * 일기/편지/답장 신고
     * [POST] /reports
     */
    public PostReportRes postReport(PostReportReq postReportReq, int reportedUserIdx) throws BaseException {
        try {
            int reportIdx = reportDao.postReport(postReportReq); // 신고 저장 -> reportIdx 반환
            return new PostReportRes(reportIdx, postReportReq.getType(), reportedUserIdx);

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
