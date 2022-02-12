package com.umc.btos.src.report;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.Constant;
import com.umc.btos.src.letter.model.PostLetterPlantRes;
import com.umc.btos.src.plant.PlantService;
import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import com.umc.btos.src.report.model.*;
import com.umc.btos.utils.JwtService;
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

    /**
     * 신고 작성 API
     * [POST] /reports
     */
    // Body에 json으로 정보를 입력받아 신고 데이터 생성
    @ResponseBody
    @PostMapping("")    // POST 어노테이션
    public BaseResponse<PostReportUserIdxPlantRes> createReport(@RequestBody PostReportReq postReportReq) {
        try{
            String ReportReason = postReportReq.getReason(); // 신고 사유 // spam : 스팸 / sex : 성적 / hate : 혐오 / dislike : 마음에 안듦 / etc : 기타
            int reportIdx = reportService.createReport(postReportReq);
             if ((ReportReason.equals("sex")) || (ReportReason.equals("hate"))) {
             // 화분 점수 감소 //-100
                 PatchModifyScoreRes ModifyScore_sex_hate = plantService.modifyScore_minus(reportService.getUserIdx(postReportReq), Constant.PLANT_LEVELDOWN_REPORT_SEX_HATE, "report");
                 PostReportUserIdxPlantRes result_sex_hate = new PostReportUserIdxPlantRes(reportIdx,postReportReq.getReportType(), reportService.getUserIdx(postReportReq), ModifyScore_sex_hate );
                 return new BaseResponse<>(result_sex_hate);
             } else if ((ReportReason.equals("spam")) || (ReportReason.equals("dislike"))) {
             // 화분 점수 감소 // -30
                 PatchModifyScoreRes ModifyScore_spam_dislike = plantService.modifyScore_minus(reportService.getUserIdx(postReportReq), Constant.PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE, "report");
                 PostReportUserIdxPlantRes result_spam_dislike = new PostReportUserIdxPlantRes(reportIdx,postReportReq.getReportType(), reportService.getUserIdx(postReportReq), ModifyScore_spam_dislike );
                 return new BaseResponse<>(result_spam_dislike);
             }
             else if ((ReportReason.equals("etc"))) {
                 PatchModifyScoreRes ModifyScore_null = new PatchModifyScoreRes(false,"report");
                 PostReportUserIdxPlantRes result_etc = new PostReportUserIdxPlantRes(reportIdx,postReportReq.getReportType(), reportService.getUserIdx(postReportReq), ModifyScore_null );
                 return new BaseResponse<>(result_etc);
             }
             else {
                 throw new BaseException(POST_REPORT_REASON);
             }
        } catch (BaseException exception){

            return new BaseResponse<>((exception.getStatus()));
        }
    }

}


