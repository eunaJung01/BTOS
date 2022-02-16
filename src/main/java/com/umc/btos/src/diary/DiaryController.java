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

import static com.umc.btos.config.BaseResponseStatus.*;

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
            // TODO : 형식적 validation - 존재하는 회원인가? & User.status = 'active'
            if (diaryProvider.checkUserIdx(postDiaryReq.getUserIdx()) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }

            diaryService.saveDiary(postDiaryReq); // 일기 저장
            PatchModifyScoreRes plantRes = plantService.modifyScore_plus(postDiaryReq.getUserIdx(), Constant.PLANT_LEVELUP_DIARY, "diary"); // 화분 점수 증가

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
            int userIdx = putDiaryReq.getUserIdx();
            int diaryIdx = putDiaryReq.getDiaryIdx();

            // TODO : 형식적 validation - 존재하는 회원인가? & User.status = 'active' / 존재하는 일기인가? / 해당 회원이 작성한 일기인가?
            if (diaryProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (diaryProvider.checkDiaryIdx(diaryIdx) == 0) {
                throw new BaseException(INVALID_DIARYIDX); // 존재하지 않는 일기입니다.
            }
            if (diaryProvider.checkUserAboutDiary(userIdx, diaryIdx) == 0) {
                throw new BaseException(INVALID_USER_ABOUT_DIARY); // 해당 일기에 접근 권한이 없는 회원입니다.
            }

            diaryService.modifyDiary(putDiaryReq); // 일기 수정

            String result = "일기(diaryIdx=" + diaryIdx + ") 수정 완료";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 삭제
     * [PATCH] /diaries/delete/:diaryIdx?userIdx=
     */
    @ResponseBody
    @PatchMapping("/delete/{diaryIdx}")
    public BaseResponse<String> deleteDiary(@PathVariable("diaryIdx") int diaryIdx, @RequestParam("userIdx") int userIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? & User.status = 'active' / 존재하는 일기인가? / 해당 회원이 작성한 일기인가?
            if (diaryProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (diaryProvider.checkDiaryIdx(diaryIdx) == 0) {
                throw new BaseException(INVALID_DIARYIDX); // 존재하지 않는 일기입니다.
            }
            if (diaryProvider.checkUserAboutDiary(userIdx, diaryIdx) == 0) {
                throw new BaseException(INVALID_USER_ABOUT_DIARY); // 해당 일기에 접근 권한이 없는 회원입니다.
            }

            diaryService.deleteDiary(diaryIdx); // 일기 삭제

            String result = "일기(diaryIdx=" + diaryIdx + ") 삭제 완료";
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

    /*
     * 현재 서버 시간 확인
     * [GET] /diaries/test
     */
    @ResponseBody
    @GetMapping("/test")
    public BaseResponse<String> dateTest() {
        try {
            String result = diaryProvider.dateTest();
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
