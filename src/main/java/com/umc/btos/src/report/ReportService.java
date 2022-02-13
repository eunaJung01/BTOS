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
    public int createReport(PostReportReq postReportReq) throws BaseException {
        try {
            int reportIdx = reportDao.createReport(postReportReq);
            return reportIdx;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 신고당한 유저의 userIdx
    public int getUserIdx(PostReportReq postReportReq) throws BaseException {
        try {
            int userIdx = reportDao.getUserIdx(postReportReq);
            return userIdx;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
