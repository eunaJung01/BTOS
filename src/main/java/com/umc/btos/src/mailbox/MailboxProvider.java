package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.DiaryProvider;
import com.umc.btos.src.diary.DiaryDao;
import com.umc.btos.src.diary.model.GetDiaryRes;
import com.umc.btos.src.mailbox.model.GetLetterRes;
import com.umc.btos.src.mailbox.model.GetMailboxRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class MailboxProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MailboxDao mailboxDao;
    private final DiaryProvider diaryProvider;
    private final DiaryDao diaryDao;

    @Autowired
    public MailboxProvider(MailboxDao mailboxDao, DiaryProvider diaryProvider, DiaryDao diaryDao) {
        this.mailboxDao = mailboxDao;
        this.diaryProvider = diaryProvider;
        this.diaryDao = diaryDao;
    }

    /*
     * 우편함 목록 조회
     * [GET] /mailboxes/:userIdx
     */
    public List<GetMailboxRes> getMailbox(int userIdx) throws BaseException {
        try {
            List<GetMailboxRes> mailbox = new ArrayList<>();
            mailbox.addAll(mailboxDao.getMailbox_diary(userIdx)); // 일기 수신 목록 - DiarySendList.receiverIdx
            mailbox.addAll(mailboxDao.getMailbox_letter(userIdx)); // 편지 수신 목록 - LetterSendList.receiverIdx
            mailbox.addAll(mailboxDao.getMailbox_reply(userIdx)); // 답장 수신 목록 - Reply.receiverIdx

            Collections.sort(mailbox); // sendAt 기준 내림차순 정렬
            return mailbox;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 우편함 - 일기 / 편지 / 답장 조회
     * [GET] /mailboxes/mail/:userIdx?type=&idx=
     * userIdx = 해당 우편을 조회하는 회원 식별자
     * type = 일기, 편지, 답장 구분 (diary / letter / reply)
     * idx = 식별자 정보 (type-idx : diary-diaryIdx / letter-letterIdx / reply-replyIdx)
     */
    public Object setMailContent(int userIdx, String type, int idx) throws BaseException {
        try {
            Object mail;
            if (type.compareTo("diary") == 0) {
                mail = diaryProvider.getDiary(userIdx, idx); // 일기 정보 저장

            } else if (type.compareTo("letter") == 0) {
                mail = mailboxDao.getLetter(idx); // 편지 정보 저장

            } else {
                mail = mailboxDao.getReply(idx); // 답장 정보 저장
            }
            return mail;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 발송자 폰트 정보 저장
    public int setSenderFontIdx(String type, int idx) throws BaseException {
        try {
            return mailboxDao.getFontIdx(type, idx);

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
