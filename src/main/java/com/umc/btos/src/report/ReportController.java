package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.report.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/btos/reports")
public class ReportController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ReportProvider reportProvider;
    @Autowired
    private final ReportService reportService;


    public ReportController(ReportProvider reportProvider, ReportService reportService) {
        this.reportProvider = reportProvider;
        this.reportService = reportService;
    }

    /**
     * 신고 작성 API
     * [POST] /btos/reports
     */
    // Body에 json으로 정보를 입력받아 신고 데이터 생성
    @ResponseBody
    @PostMapping("")    // POST 어노테이션
    public BaseResponse<PostReportRes> createReport(@RequestBody PostReportReq postReportReq) {

        try{
            PostReportRes postReportRes = reportService.createReport(postReportReq);
            return new BaseResponse<>(postReportRes);
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }



}
