package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.mailbox.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mailboxes")
public class MailboxController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final MailboxProvider mailboxProvider;
    @Autowired
    private final MailboxService mailboxService;

    public MailboxController(MailboxProvider mailboxProvider, MailboxService mailboxService) {
        this.mailboxProvider = mailboxProvider;
        this.mailboxService = mailboxService;
    }

    /*
     * 우편함 - 일기 / 편지 / 답장 조회
     * [GET] /mailboxes/mail?type=&idx=
     * type = 일기, 편지, 답장 구분 (diary / letter / reply)
     * idx = 식별자 정보 (type-idx : diary-diaryIdx / letter-letterIdx / reply-replyIdx)
     */
    @ResponseBody
    @GetMapping("/mail")
    public BaseResponse<GetMailRes> getDiary(@RequestParam("type") String type, @RequestParam("idx") int idx) {
        try {
            GetMailRes getMail = new GetMailRes(type);
            getMail.setContent(mailboxProvider.setMailContent(type, idx));
            getMail.setSenderFontIdx(mailboxProvider.setSenderFontIdx(type, idx));
            return new BaseResponse<>(getMail);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
