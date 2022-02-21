package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.Constant;
import com.umc.btos.src.plant.PlantService;
import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import com.umc.btos.src.report.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/reports")
public class ReportController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ReportProvider reportProvider;
    @Autowired
    private final ReportService reportService;
    @Autowired
    private final PlantService plantService;


    public ReportController(ReportProvider reportProvider, ReportService reportService, PlantService plantService) {
        this.reportProvider = reportProvider;
        this.reportService = reportService;
        this.plantService = plantService;
    }

    /*
     * 일기/편지/답장 신고
     * [POST] /reports
     * 1. 신고 접수
     * 2. 화분 점수와 단계 변경
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostReportRes> postReport(@RequestBody PostReportReq postReportReq) {
        try {
            // TODO : 형식적 validation - type, typeIdx, reason

            // 신고를 당한 회원 식별자
            int reportedUserIdx = reportProvider.getReportedUserIdx(postReportReq.getType(), postReportReq.getTypeIdx());

            // 신고 저장
            PostReportRes postReportRes = reportService.postReport(postReportReq, reportedUserIdx);

            // 화분 점수와 단계 변경
            String reason = postReportReq.getReason();

            // reason = sex, hate
            if (reason.equals("sex") || reason.equals("hate")) {
                postReportRes.setPatchModifyScoreRes(plantService.modifyScore_minus(reportedUserIdx, Constant.PLANT_LEVELDOWN_REPORT_SEX_HATE, "report"));
            }
            // reason = spam, dislike
            else {
                postReportRes.setPatchModifyScoreRes(plantService.modifyScore_minus(reportedUserIdx, Constant.PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE, "report"));
            }

            return new BaseResponse<>(postReportRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
