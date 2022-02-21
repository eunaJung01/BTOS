package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class ReportProvider {
    private final ReportDao reportDao;
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ReportProvider(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    // 신고를 당한 회원 식별자 반환
    public int getReportedUserIdx(String type, int typeIdx) throws BaseException {
        try {
            return reportDao.getReportedUserIdx(type, typeIdx);

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
