package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.Constant;
import com.umc.btos.src.letter.model.*;
import com.umc.btos.src.plant.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/letters")
public class LetterController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private final LetterProvider letterProvider;
    @Autowired
    private final LetterService letterService;
    @Autowired
    private final PlantService plantService;

    public LetterController(LetterProvider letterProvider, LetterService letterService, PlantService plantService) {
        this.letterProvider = letterProvider;
        this.letterService = letterService;
        this.plantService = plantService;
    }

    /*
     * 편지 저장 및 발송
     * [POST] /letters
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<String> postLetter(@RequestBody PostLetterReq postLetterReq) {

        try {
            // TODO : 형식적 validation - 회원 존재 여부 확인
            if (letterProvider.checkUserIdx(postLetterReq.getUserIdx()) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }

            // 편지 저장 및 발송
            int letterIdx = letterService.postLetter(postLetterReq);

            // 화분 점수 증가
            plantService.modifyScore_plus(postLetterReq.getUserIdx(), Constant.PLANT_LEVELUP_LETTER, "letter");

            String result = "편지 저장 및 발송이 완료되었습니다. (letterIdx = " + letterIdx + ")";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }

    }

    /*
     * 편지 삭제
     * [PATCH] /letters/delete/:letterIdx?userIdx=
     */
    @ResponseBody
    @PatchMapping("/delete/{letterIdx}")
    public BaseResponse<String> deleteLetter(@PathVariable("letterIdx") int letterIdx, @RequestParam("userIdx") int userIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? / 존재하는 편지인가? / 해당 회원이 작성한 편지인가?
            if (letterProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (letterProvider.checkLetterIdx(letterIdx) == 0) {
                throw new BaseException(INVALID_LETTERIDX); // 존재하지 않는 편지입니다.
            }
            if (letterProvider.checkUserAboutLetter(userIdx, letterIdx) == 0) {
                throw new BaseException(INVALID_USER_ABOUT_LETTER); // 해당 편지에 접근 권한이 없는 회원입니다.
            }

            letterService.deleteLetter(letterIdx);
            String result = "편지(letterIdx = " + letterIdx + ")가 삭제되었습니다.";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /*
     * 편지 조회
     * [GET] /letters/:letterIdx?userIdx=
     * 편지 열람 여부 변경 (LetterSendList.isChecked : 0 -> 1)
     */
    @ResponseBody
    @GetMapping("/{letterIdx}")
    public BaseResponse<GetLetterRes> getLetter(@PathVariable("letterIdx") int letterIdx, @RequestParam("userIdx") int userIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? / 존재하는 편지인가? / 해당 회원이 작성한 편지인가?
            if (letterProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (letterProvider.checkLetterIdx(letterIdx) == 0) {
                throw new BaseException(INVALID_LETTERIDX); // 존재하지 않는 편지입니다.
            }
            if (letterProvider.checkUserAboutLetter(userIdx, letterIdx) == 0) {
                throw new BaseException(INVALID_USER_ABOUT_LETTER); // 해당 편지에 접근 권한이 없는 회원입니다.
            }

            GetLetterRes getLetterRes = letterProvider.getLetter(userIdx, letterIdx);
            return new BaseResponse<>(getLetterRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
