package com.umc.btos.src.letter;


import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.Constant;
import com.umc.btos.src.letter.model.*;
import com.umc.btos.src.plant.PlantService;
import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/letters")
public class LetterController {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기
    @Autowired
    private final LetterProvider letterProvider;
    @Autowired
    private final LetterService letterService;
    @Autowired
    private final PlantService plantService;



    public LetterController(LetterProvider letterProvider, LetterService letterService,PlantService plantService) {
        this.letterProvider = letterProvider;
        this.letterService = letterService;
        this.plantService = plantService;
    }


    /**
     * 편지 작성 API
     * [POST] /letters
     */

    // Body에 json 파일 형식으로 넣음
    @ResponseBody
    @PostMapping("")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PatchModifyScoreRes> createLetter(@RequestBody PostLetterReq postLetterReq) {

        try{
            letterService.createLetter(postLetterReq); // letter, letterSendList에 컬럼 추가
            // 화분 점수 증가
            PatchModifyScoreRes result = plantService.modifyScore_plus(postLetterReq.getUserIdx(), Constant.PLANT_LEVELUP_LETTER,"letter");
            return new BaseResponse<>(result);
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }

    /**
     * 편지 조회 API
     * [GET] /letters/:letterIdx
    */
    // Path-variable - letterIdx를 인수로 받아 해당 인덱스의 letter을 불러온다.
    @ResponseBody
    @GetMapping("/{letterIdx}") // (GET) localhost:9000/btos/letters/:letterIdx
    public BaseResponse<GetLetterRes> getLetter(@PathVariable("letterIdx") int letterIdx) {
        // @PathVariable RESTful(URL)에서 명시된 파라미터({})를 받는 어노테이션, 이 경우 letterIdx값을 받아옴.
        // Get Letters
        try {
            GetLetterRes getLetterRes = letterProvider.getLetter(letterIdx);
            return new BaseResponse<>(getLetterRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }

    /**
     * 편지 삭제 API
     * [PATCH] /letters/:letterIdx
     */

    @ResponseBody
    @PatchMapping("/{letterIdx}")
    // Path-variable - letterIdx를 파라미터로 받음 - 해당 letterIdx의 status를 deleted로 변경
    public BaseResponse<String> deleteLetter(@PathVariable("letterIdx") int letterIdx) {  //반환형은 string
        try {

            PatchLetterReq patchLetterReq = new PatchLetterReq(letterIdx);
            letterService.modifyLetterStatus(patchLetterReq);
            String result = "편지가 삭제되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


}
