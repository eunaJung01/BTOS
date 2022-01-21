package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.src.diary.model.*;
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

    public DiaryController(DiaryProvider diaryProvider, DiaryService diaryService) {
        this.diaryProvider = diaryProvider;
        this.diaryService = diaryService;
    }

    /*
     * 일기 작성 여부 확인
     * [GET] /diaries/:userIdx/:date
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
     * [POST] /diaries
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
     * [PUT] /diaries
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
     * [PATCH] /diaries/:diaryIdx
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

    /*
     * Archive 조회 - 캘린더
     * [GET] /diaries/calendar?userIdx=&date=&type
     * date = YYYY-MM
     * type (조회 방식) = 1. doneList : 나뭇잎 색으로 done list 개수 표현 / 2. emotion : 감정 이모티콘
     */
    @ResponseBody
    @GetMapping("/calendar")
    public BaseResponse<List<GetCalendarRes>> getCalendar(@RequestParam("userIdx") int userIdx, @RequestParam("date") String date, @RequestParam("type") String type) {
        try {
            List<GetCalendarRes> getCalendarRes = diaryProvider.getCalendar(userIdx, date, type);
            return new BaseResponse<>(getCalendarRes);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * Archive 조회 - 달별 일기 리스트
     * [GET] /diaries/diarylist?userIdx=&date=
     * date = YYYY-MM
     * 최신순 정렬
     */
    @ResponseBody
    @GetMapping("/diaryList")
    public BaseResponse<List<GetDiaryRes>> getDiaryList(@RequestParam("userIdx") int userIdx, @RequestParam("date") String date) {
        try {
            List<GetDiaryRes> diaryList = diaryProvider.getDiaryList(userIdx, date);
            return new BaseResponse<>(diaryList);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 조회
     * [GET] /diaries?diaryIdx=
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<GetDiaryRes> getDiary(@RequestParam("diaryIdx") int diaryIdx) {
        try {
            GetDiaryRes diary = diaryProvider.getDiary(diaryIdx);
            return new BaseResponse<>(diary);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
