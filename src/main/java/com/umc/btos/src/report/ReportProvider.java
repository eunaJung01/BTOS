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
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ReportProvider(ReportDao reportDao) {
        this.reportDao = reportDao;
    }
}
