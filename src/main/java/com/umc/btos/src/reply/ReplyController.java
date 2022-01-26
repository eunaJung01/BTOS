package com.umc.btos.src.reply;



import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.reply.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/replies")
public class ReplyController {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기
    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    private final ReplyProvider replyProvider;
    @Autowired
    private final ReplyService replyService;
    @Autowired
    private final JwtService jwtService;

    public ReplyController(ReplyProvider replyProvider, ReplyService replyService, JwtService jwtService) {
        this.replyProvider = replyProvider;
        this.replyService = replyService;
        this.jwtService = jwtService;
    }
    /**
     * 답장 작성 API
     * [POST] /replies
     */
    // Body
    @ResponseBody
    @PostMapping("")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostReplyRes> createReply(@RequestBody PostReplyReq postReplyReq) {
        //  @RequestBody란, 클라이언트가 전송하는 HTTP Request Body(우리는 JSON으로 통신하니, 이 경우 body는 JSON)를 자바 객체로 매핑시켜주는 어노테이션
        try{
            PostReplyRes postReplyRes = replyService.createReply(postReplyReq);
            return new BaseResponse<>(postReplyRes);
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }
    /**
     * 답장 조회 API
     * [GET] /replies/:replyIdx
     */
    // Path-variable
    @ResponseBody
    @GetMapping("/{replyIdx}") // (GET) localhost:9000/btos/replies/:replyIdx
    public BaseResponse<GetReplyRes> getReply(@PathVariable("replyIdx") int replyIdx) {
        // @PathVariable RESTful(URL)에서 명시된 파라미터({})를 받는 어노테이션, 이 경우 letterIdx값을 받아옴.
        // Get Letters
        try {
            GetReplyRes getReplyRes = replyProvider.getReply(replyIdx);
            return new BaseResponse<>(getReplyRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }
    /**
     * 답장 삭제 API
     * [PATCH] /replies/:replyIdx
     */
    @ResponseBody
    @PatchMapping("/{replyIdx}")
    // Path-variable
    public BaseResponse<String> deleteReply(@PathVariable("replyIdx") int replyIdx) {
        try {

            PatchReplyReq patchReplyReq = new PatchReplyReq(replyIdx);
            replyService.modifyReplyStatus(patchReplyReq);
            String result = "답장이 삭제되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
