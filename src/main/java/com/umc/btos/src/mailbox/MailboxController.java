package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.mailbox.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mailboxes")
public class MailboxController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final MailboxProvider mailboxProvider;

    public MailboxController(MailboxProvider mailboxProvider) {
        this.mailboxProvider = mailboxProvider;
    }

    /*
     * 우편함 목록 조회
     * [GET] /mailboxes/:userIdx
     */
    @ResponseBody
    @GetMapping("/{userIdx}")
    public BaseResponse<List<GetMailboxRes>> getMailbox(@PathVariable("userIdx") int userIdx) {
        try {
            List<GetMailboxRes> mailbox = mailboxProvider.getMailbox(userIdx);
            return new BaseResponse<>(mailbox);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 우편함 - 일기 / 편지 / 답장 조회
     * [GET] /mailboxes/mail/:userIdx?type=&idx=
     * userIdx = 해당 우편을 조회하는 회원 식별자
     * type = 일기, 편지, 답장 구분 (diary / letter / reply)
     * idx = 식별자 정보 (type-idx : diary-diaryIdx / letter-letterIdx / reply-replyIdx)
     */
    @ResponseBody
    @GetMapping("/mail/{userIdx}")
    public BaseResponse<GetMailRes> getMail(@PathVariable("userIdx") int userIdx, @RequestParam("type") String type, @RequestParam("idx") int idx) {
        try {
            GetMailRes mail = new GetMailRes(type);
            mailboxProvider.setMailRes_sender(mail, type, idx); // 발신인 정보 저장
            mail.setContent(mailboxProvider.setMailContent(userIdx, type, idx));

            return new BaseResponse<>(mail);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
