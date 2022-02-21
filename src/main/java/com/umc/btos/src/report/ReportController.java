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

import static com.umc.btos.config.BaseResponseStatus.POST_REPORT_REASON;

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
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostReportRes> createReport(@RequestBody PostReportReq postReportReq) {
        try{
//            // 신고 사유
//            // spam : 스팸 / sex : 성적 / hate : 혐오 / dislike : 마음에 안듦 / etc : 기타
//            String ReportReason = postReportReq.getReason();
//
//            // 신고 생성 // Report 테이블에 값 추가
//            int reportIdx = reportService.createReport(postReportReq);
//
//            // 신고 사유 : sex, hate
//             if ((ReportReason.equals("sex")) || (ReportReason.equals("hate"))) {
//                 // 화분 점수 감소 //-100
//                 PatchModifyScoreRes ModifyScore_sex_hate = plantService.modifyScore_minus(reportService.getUserIdx(postReportReq), Constant.PLANT_LEVELDOWN_REPORT_SEX_HATE, "report");
//                 PostReportUserIdxPlantRes result_sex_hate = new PostReportUserIdxPlantRes(reportIdx,postReportReq.getReportType(), reportService.getUserIdx(postReportReq), ModifyScore_sex_hate );
//                 return new BaseResponse<>(result_sex_hate);
//             }
//
//             // 신고 사유 : spam, dislike
//             else if ((ReportReason.equals("spam")) || (ReportReason.equals("dislike"))) {
//                 // 화분 점수 감소 // -30
//                 PatchModifyScoreRes ModifyScore_spam_dislike = plantService.modifyScore_minus(reportService.getUserIdx(postReportReq), Constant.PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE, "report");
//                 PostReportUserIdxPlantRes result_spam_dislike = new PostReportUserIdxPlantRes(reportIdx,postReportReq.getReportType(), reportService.getUserIdx(postReportReq), ModifyScore_spam_dislike );
//                 return new BaseResponse<>(result_spam_dislike);
//             }
//
//             // 신고 사유 : etc
//             else if ((ReportReason.equals("etc"))) {
//                 // 화분 점수 감소 // 없음
//                 PatchModifyScoreRes ModifyScore_null = new PatchModifyScoreRes(false,"report");
//                 PostReportUserIdxPlantRes result_etc = new PostReportUserIdxPlantRes(reportIdx,postReportReq.getReportType(), reportService.getUserIdx(postReportReq), ModifyScore_null );
//                 return new BaseResponse<>(result_etc);
//             }
//             else {
//                 // ERROR : 8009 - 신고의 사유가 정해진 사유를 벗어납니다.
//                 throw new BaseException(POST_REPORT_REASON);
//             }
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}


