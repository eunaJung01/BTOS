package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.Constant;
import com.umc.btos.src.plant.PlantDao;
import com.umc.btos.src.plant.PlantService;
import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import com.umc.btos.src.report.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private final PlantDao plantDao;


    public ReportController(ReportProvider reportProvider, ReportService reportService, PlantService plantService, PlantDao plantDao) {
        this.reportProvider = reportProvider;
        this.reportService = reportService;
        this.plantService = plantService;
        this.plantDao = plantDao;
    }

    /*
     * 일기/편지/답장 신고
     * [POST] /reports
     * 1. 신고 저장
     * 2. 화분 점수와 단계 변경
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostReportRes> postReport(@RequestBody PostReportReq postReportReq) {
        try {
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
            else if (reason.equals("spam") || reason.equals("dislike")) {
                postReportRes.setPatchModifyScoreRes(plantService.modifyScore_minus(reportedUserIdx, Constant.PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE, "report"));
            }
            // reason = etc
            else {
                int plantLevel = plantDao.getLevel(reportedUserIdx);
                postReportRes.setPatchModifyScoreRes(new PatchModifyScoreRes("reply", null, false, plantLevel));
            }

            return new BaseResponse<>(postReportRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
