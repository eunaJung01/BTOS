package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.diary.model.GetDiaryRes;
import com.umc.btos.src.mailbox.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/btos/mailbox")
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
     * 우편함 - 일기 조회
     * [GET] /btos/mailbox?diaryIdx=
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<GetDiaryRes_Mailbox> getDiary(@RequestParam("diaryIdx") int diaryIdx) {
        try {
            GetDiaryRes_Mailbox diary = mailboxProvider.getDiary(diaryIdx);
            return new BaseResponse<>(diary);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
