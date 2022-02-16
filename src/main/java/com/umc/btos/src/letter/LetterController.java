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
    public BaseResponse<PostLetterRes> postLetter(@RequestBody PostLetterReq postLetterReq) {

        try {
            // TODO : 형식적 validation - 회원 존재 여부 확인
            if (letterProvider.checkUserIdx(postLetterReq.getUserIdx()) == 0) {
                throw new BaseException(LETTER_INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }

            // 편지 저장 및 발송
            PostLetterRes postLetterRes = letterService.postLetter(postLetterReq);

            // 화분 점수 증가
            postLetterRes.setPlantRes(plantService.modifyScore_plus(postLetterReq.getUserIdx(), Constant.PLANT_LEVELUP_LETTER, "letter"));

            return new BaseResponse<>(postLetterRes);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }

    }

    /*
     * 편지 삭제
     * [PATCH] /letters/:letterIdx
     */
    @ResponseBody
    @PatchMapping("/{letterIdx}")
    public BaseResponse<String> deleteLetter(@PathVariable("letterIdx") int letterIdx) {
        try {
            letterService.deleteLetter(letterIdx);
            String result = "편지(letterIdx = " + letterIdx + ")가 삭제되었습니다.";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /*
     * 편지 조회
     * [GET] /letters/:letterIdx/:userIdx
     */
    @ResponseBody // userIdx는 이 API를 호출하는 편지를 읽는 유저
    @GetMapping("/{letterIdx}/{userIdx}")
    public BaseResponse<GetLetterRes> getLetter(@PathVariable("letterIdx") int letterIdx, @PathVariable("userIdx") int userIdx) {
        try {
            GetLetterRes getLetterRes = letterProvider.getLetter(letterIdx, userIdx);
            return new BaseResponse<>(getLetterRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
