package com.umc.btos.src.reply;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.reply.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/replies")
public class ReplyController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ReplyProvider replyProvider;
    @Autowired
    private final ReplyService replyService;

    public ReplyController(ReplyProvider replyProvider, ReplyService replyService) {
        this.replyProvider = replyProvider;
        this.replyService = replyService;
    }

    /*
     * 답장 저장 및 발송
     * [POST] /replies
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostReplyRes> postReply(@RequestBody PostReplyReq postReplyReq) {
        try {
            PostReplyRes postReplyRes = replyService.postReply(postReplyReq);
            return new BaseResponse<>(postReplyRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /*
     * 답장 삭제
     * [PATCH] /replies/:replyIdx?userIdx=
     */
    @ResponseBody
    @PatchMapping("/{replyIdx}")
    public BaseResponse<String> deleteReply(@PathVariable("replyIdx") int replyIdx, @RequestParam("userIdx") int userIdx) {
        try {
            replyService.deleteReply(replyIdx);
            String result = "답장(replyIdx = "+replyIdx+")이 삭제되었습니다.";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /*
     * 답장 조회 API
     * [GET] /replies/:replyIdx
     */
    @ResponseBody
    @GetMapping("/{replyIdx}")
    public BaseResponse<GetReplyRes> getReply(@PathVariable("replyIdx") int replyIdx) {
        try {
            GetReplyRes getReplyRes = replyProvider.getReply(replyIdx);
            return new BaseResponse<>(getReplyRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
