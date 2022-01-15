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
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    private final ReportProvider reportProvider;
    @Autowired
    private final ReportService reportService;
    @Autowired
    private final JwtService jwtService;

    public ReportController(ReportProvider reportProvider, ReportService reportService, JwtService jwtService) {
        this.reportProvider = reportProvider;
        this.reportService = reportService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    /**
     * 신고 작성 API
     * [POST] /btos/reports
     */
    // Body
    @ResponseBody
    @PostMapping("")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostReportRes> createReport(@RequestBody PostReportReq postReportReq) {
        //  @RequestBody란, 클라이언트가 전송하는 HTTP Request Body(우리는 JSON으로 통신하니, 이 경우 body는 JSON)를 자바 객체로 매핑시켜주는 어노테이션
        try{
            PostReportRes postReportRes = reportService.createReport(postReportReq);
            return new BaseResponse<>(postReportRes);
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }



}
