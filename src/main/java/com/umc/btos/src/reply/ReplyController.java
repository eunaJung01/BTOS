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
    public BaseResponse<String> postReply(@RequestBody PostReplyReq postReplyReq) {
        try {
            int replyIdx = replyService.postReply(postReplyReq);

            String result = "답장 저장 및 발송이 완료되었습니다. (replyIdx = " + replyIdx + ")";
            return new BaseResponse<>(result);

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
            // TODO : 형식적 validation - 존재하는 회원인가? / 존재하는 답장인가? / 해당 회원이 작성한 답장인가?
            if (replyProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (replyProvider.checkReplyIdx(replyIdx) == 0) {
                throw new BaseException(INVALID_LETTERIDX); // 존재하지 않는 답장입니다.
            }
            if (replyProvider.checkUserAboutReply(userIdx, replyIdx) == 0) {
                throw new BaseException(INVALID_USER_ABOUT_REPLY); // 해당 답장에 접근 권한이 없는 회원입니다.
            }

            replyService.deleteReply(replyIdx);
            String result = "답장(replyIdx = " + replyIdx + ")이 삭제되었습니다.";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /*
     * 답장 조회
     * [GET] /replies/:replyIdx?userIdx=
     * 답장 열람 여부 변경 (Reply.isChecked : 0 -> 1)
     */
    @ResponseBody
    @GetMapping("/{replyIdx}")
    public BaseResponse<GetReplyRes> getReply(@PathVariable("replyIdx") int replyIdx, @RequestParam("userIdx") int userIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? / 존재하는 답장인가? / 해당 회원이 작성한 답장인가?
            if (replyProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (replyProvider.checkReplyIdx(replyIdx) == 0) {
                throw new BaseException(INVALID_LETTERIDX); // 존재하지 않는 답장입니다.
            }
            if (replyProvider.checkUserAboutReply(userIdx, replyIdx) == 0) {
                throw new BaseException(INVALID_USER_ABOUT_REPLY); // 해당 답장에 접근 권한이 없는 회원입니다.
            }

            GetReplyRes getReplyRes = replyProvider.getReply(replyIdx);
            return new BaseResponse<>(getReplyRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
