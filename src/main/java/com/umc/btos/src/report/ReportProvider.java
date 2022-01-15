package com.umc.btos.src.report;
import com.umc.btos.src.report.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportProvider {
    private final ReportDao reportDao;
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public ReportProvider(ReportDao reportDao, JwtService jwtService) {
        this.reportDao = reportDao;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }
}
