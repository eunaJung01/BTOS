package com.umc.btos.src.report;
import com.umc.btos.config.BaseException;

import com.umc.btos.src.report.model.*;

import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;


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
    // ******************************************************************************
    // 신고 작성(POST)
    /**
    public PostReportRes createReport(PostReportReq postReportReq) throws BaseException {

        try {
            int reportIdx = reportDao.createReport(postReportReq);
            return new PostReportRes(reportIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.

            throw new BaseException(DATABASE_ERROR);
        }
    }*/
}
