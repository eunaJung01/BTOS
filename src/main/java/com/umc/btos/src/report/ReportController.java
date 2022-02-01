package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.Constant;
import com.umc.btos.src.plant.PlantService;
import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import com.umc.btos.src.report.model.*;
import com.umc.btos.utils.JwtService;
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


    public ReportController(ReportProvider reportProvider, ReportService reportService, PlantService plantService) {
        this.reportProvider = reportProvider;
        this.reportService = reportService;
        this.plantService = plantService;
    }

    /**
     * 신고 작성 API
     * [POST] /reports
     */
    // Body에 json으로 정보를 입력받아 신고 데이터 생성
    @ResponseBody
    @PostMapping("")    // POST 어노테이션
    public BaseResponse<String> createReport(@RequestBody PostReportReq postReportReq) {
        try{
            String ReportReason = postReportReq.getReason(); // 신고 사유 // spam : 스팸 / sex : 성적 / hate : 혐오 / dislike : 마음에 안듦 / etc : 기타
            reportService.createReport(postReportReq);
             if ((ReportReason.equals("sex")) || (ReportReason.equals("hate"))) {
             // 화분 점수 감소 //-100
                plantService.modifyScore_minus(reportService.getUserIdx(postReportReq), Constant.PLANT_LEVELDOWN_REPORT_SEX_HATE, "report_sex_hate");
             } else if ((ReportReason.equals("spam")) || (ReportReason.equals("dislike"))) {
             // 화분 점수 감소 // -30
                plantService.modifyScore_minus(reportService.getUserIdx(postReportReq), Constant.PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE, "report_spam_dislike");
             }
             else if ((ReportReason.equals("etc"))) {
             String result = "신고-(기타)가 완료되었습니다.";
             return new BaseResponse<>(result);
             }
            String result = "신고가 완료되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception){

            return new BaseResponse<>((exception.getStatus()));
        }
    }

}


