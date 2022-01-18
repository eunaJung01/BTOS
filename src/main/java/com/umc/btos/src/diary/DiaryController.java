package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.src.diary.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/btos/diary")
public class DiaryController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final DiaryProvider diaryProvider;
    @Autowired
    private final DiaryService diaryService;

    public DiaryController(DiaryProvider diaryProvider, DiaryService diaryService) {
        this.diaryProvider = diaryProvider;
        this.diaryService = diaryService;
    }

    /*
     * 일기 작성 여부 확인
     * [GET] /btos/diary/:userIdx/:date
     */
    @ResponseBody
    @GetMapping("/{userIdx}/{date}")
    public BaseResponse<GetCheckDiaryRes> checkDiary(@PathVariable("userIdx") int userIdx, @PathVariable("date") String date) {
        try {
            GetCheckDiaryRes getCheckDiaryRes = diaryProvider.checkDiary(userIdx, date);
            return new BaseResponse<>(getCheckDiaryRes);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 저장
     * [POST] /btos/diary
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostDiaryRes> saveDiary(@RequestBody PostDiaryReq postDiaryReq) {
        try {
            PostDiaryRes postDiaryRes = diaryService.saveDiary(postDiaryReq);
            return new BaseResponse<>(postDiaryRes);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 수정
     * [PUT] /btos/diary
     */
    @ResponseBody
    @PutMapping("")
    public BaseResponse<String> modifyDiary(@RequestBody PutDiaryReq putDiaryReq) {
        try {
            diaryService.modifyDiary(putDiaryReq);

            String result = "일기 - diaryIdx=" + putDiaryReq.getDiaryIdx() + " 수정 완료";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 삭제
     * [PATCH] /btos/diary/:diaryIdx
     */
    @ResponseBody
    @PatchMapping("/{diaryIdx}")
    public BaseResponse<String> deleteDiary(@PathVariable("diaryIdx") int diaryIdx) {
        try {
            diaryService.deleteDiary(diaryIdx);

            String result = "일기 - diaryIdx=" + diaryIdx + " 삭제 완료";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}