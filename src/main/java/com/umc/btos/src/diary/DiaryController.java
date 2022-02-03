package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.src.diary.model.*;
import com.umc.btos.src.plant.PlantService;
import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diaries")
public class DiaryController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final DiaryProvider diaryProvider;
    @Autowired
    private final DiaryService diaryService;
    @Autowired
    private final PlantService plantService;

    public DiaryController(DiaryProvider diaryProvider, DiaryService diaryService, PlantService plantService) {
        this.diaryProvider = diaryProvider;
        this.diaryService = diaryService;
        this.plantService = plantService;
    }

    /*
     * 일기 작성 여부 확인
     * [GET] /diaries/:userIdx/:date
     */
    @ResponseBody
    @GetMapping("/{userIdx}/{date}")
    public BaseResponse<GetCheckDiaryRes> checkDiary(@PathVariable("userIdx") int userIdx, @PathVariable("date") String date) {
        try {
            GetCheckDiaryRes getCheckDiaryRes = diaryProvider.checkDiaryDate(userIdx, date);
            return new BaseResponse<>(getCheckDiaryRes);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 저장 및 화분 점수와 단계 변경
     * [POST] /diaries
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PatchModifyScoreRes> saveDiary(@RequestBody PostDiaryReq postDiaryReq) {
        try {
            diaryService.saveDiary(postDiaryReq);
            PatchModifyScoreRes plantRes = plantService.modifyScore_plus(postDiaryReq.getUserIdx(), Constant.PLANT_LEVELUP_DIARY, "diary");

            return new BaseResponse<>(plantRes);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 수정
     * [PUT] /diaries
     */
    @ResponseBody
    @PutMapping("")
    public BaseResponse<String> modifyDiary(@RequestBody PutDiaryReq putDiaryReq) {
        try {
            diaryService.modifyDiary(putDiaryReq);

            String result = "일기(diaryIdx=" + putDiaryReq.getDiaryIdx() + ") 수정 완료";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 삭제
     * [PATCH] /diaries/delete/:diaryIdx
     */
    @ResponseBody
    @PatchMapping("/delete/{diaryIdx}")
    public BaseResponse<String> deleteDiary(@PathVariable("diaryIdx") int diaryIdx) {
        try {
            diaryService.deleteDiary(diaryIdx);

            String result = "일기-diaryIdx=" + diaryIdx + " 삭제 완료";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 발송 리스트 조회
     * 매일 18:59:59 Firebase에서 호출
     * [GET] /diaries/diarySendList
     */
    @ResponseBody
    @GetMapping("/diarySendList")
    public BaseResponse<List<GetSendListRes>> getDiarySendList() {
        try {
            List<GetSendListRes> diarySendList = diaryProvider.getDiarySendList();
            return new BaseResponse<>(diarySendList);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
     }

    /*
     * 일기 조회
     * [GET] /diaries/:diaryIdx
     */
//    @ResponseBody
//    @GetMapping("/{diaryIdx}")
//    public BaseResponse<GetDiaryRes> getDiary(@PathVariable("diaryIdx") int diaryIdx) {
//        try {
//            GetDiaryRes diary = diary.getDiary(diaryIdx);
//            return new BaseResponse<>(diary);
//
//        } catch (BaseException exception) {
//            return new BaseResponse<>(exception.getStatus());
//        }
//    }

}
