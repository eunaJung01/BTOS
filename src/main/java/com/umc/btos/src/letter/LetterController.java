package com.umc.btos.src.letter;


import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.letter.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/btos/letters")
public class LetterController {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기
    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    private final LetterProvider letterProvider;
    @Autowired
    private final LetterService letterService;
    @Autowired
    private final JwtService jwtService;


    public LetterController(LetterProvider letterProvider, LetterService letterService, JwtService jwtService) {
        this.letterProvider = letterProvider;
        this.letterService = letterService;
        this.jwtService = jwtService;
    }

    /**
     * 편지 작성 API
     * [POST] /btos/letters
     */
    // Body
    @ResponseBody
    @PostMapping("")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostLetterRes> createLetter(@RequestBody PostLetterReq postLetterReq) {
        //  @RequestBody란, 클라이언트가 전송하는 HTTP Request Body(우리는 JSON으로 통신하니, 이 경우 body는 JSON)를 자바 객체로 매핑시켜주는 어노테이션
        try{
            PostLetterRes postLetterRes = letterService.createLetter(postLetterReq);
            return new BaseResponse<>(postLetterRes);
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }

    /**
     * 편지 조회 API
     * [GET] /btos/letters/:letterIdx
     */
    // Path-variable
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
     * [PATCH] /btos/letters/:letterIdx
     */
    @ResponseBody
    @PatchMapping("/{letterIdx}")
    // Path-variable
    public BaseResponse<String> deleteLetter(@PathVariable("letterIdx") int letterIdx) {
        try {

            PatchLetterReq patchLetterReq = new PatchLetterReq(letterIdx);
            letterService.modifyLetterStaus(patchLetterReq);
            String result = "편지가 삭제되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }



}
