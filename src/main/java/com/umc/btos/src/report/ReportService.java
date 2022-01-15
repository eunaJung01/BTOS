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
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분: Log를 기록하기 위해 필요한 함수입니다.

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final ReportDao reportDao;
    private final ReportProvider reportProvider;
    private final JwtService jwtService;


    @Autowired //readme 참고
    public ReportService(ReportDao reportDao, ReportProvider reportProvider, JwtService jwtService) {
        this.reportDao = reportDao;
        this.reportProvider = reportProvider;
        this.jwtService = jwtService;

    }
    // ******************************************************************************
    // 신고 작성(POST)

    public PostReportRes createReport(PostReportReq postReportReq) throws BaseException {

        try {
            int reportIdx = reportDao.createReport(postReportReq);
            return new PostReportRes(reportIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
