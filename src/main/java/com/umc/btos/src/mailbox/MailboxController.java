package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.mailbox.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

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
            // TODO : 형식적 validation - 존재하는 회원인가? & User.status = 'active'
            if (mailboxProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않거나 탈퇴한 회원입니다.
            }

            List<GetMailboxRes> mailbox = mailboxProvider.getMailbox(userIdx);
            return new BaseResponse<>(mailbox);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 우편함 - 일기 / 편지 / 답장 조회
     * [GET] /mailboxes/mail?userIdx?type=&typeIdx=
     * userIdx = 해당 우편을 조회하는 회원 식별자
     * type = 일기, 편지, 답장 구분 (diary / letter / reply)
     * typeIdx = 식별자 정보 (type-typeIdx : diary-diaryIdx / letter-letterIdx / reply-replyIdx)
     */
    @ResponseBody
    @GetMapping("/mail")
    public BaseResponse<GetMailRes> getMail(@RequestParam("userIdx") int userIdx, @RequestParam("type") String type, @RequestParam("typeIdx") int typeIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? & User.status = 'active' / type(diary, letter, reply) 입력 확인 / 해당 type에 존재하는 typeIdx인가?
            if (mailboxProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않거나 탈퇴한 회원입니다.
            }
            if (type.compareTo("diary") == 0 && type.compareTo("letter") == 0 && type.compareTo("reply") == 0) {
                throw new BaseException(INVALID_TYPE); // 잘못된 type 입니다. (diary, letter, reply 중 1)
            }
            if (mailboxProvider.checkTypeIdx(type, typeIdx) == 0) {
                throw new BaseException(INVALID_TYPEIDX_ABOUT_TYPE); // 해당 type에 존재하지 않는 typeIdx 입니다.
            }

            GetMailRes mail = mailboxProvider.getMail(userIdx, type, typeIdx); // 우편 내용, 발신인 정보 저장

            return new BaseResponse<>(mail);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
