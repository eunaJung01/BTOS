package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.DiaryProvider;
import com.umc.btos.src.letter.LetterProvider;
import com.umc.btos.src.mailbox.model.GetMailRes;
import com.umc.btos.src.mailbox.model.GetMailboxRes;
import com.umc.btos.src.reply.ReplyProvider;
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
    private final LetterProvider letterProvider;
    private final ReplyProvider replyProvider;

    @Autowired
    public MailboxProvider(MailboxDao mailboxDao, DiaryProvider diaryProvider, LetterProvider letterProvider, ReplyProvider replyProvider) {
        this.mailboxDao = mailboxDao;
        this.diaryProvider = diaryProvider;
        this.letterProvider = letterProvider;
        this.replyProvider = replyProvider;
    }

    /*
     * 존재하는 회원인지 확인
     */
    public int checkUserIdx(int userIdx) throws BaseException {
        try {
            return mailboxDao.checkUserIdx(userIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 해당 type에 존재하는 typeIdx인지 확인
     */
    public int checkTypeIdx(String type, int typeIdx) throws BaseException {
        try {
            if (type.compareTo("diary") == 0) {
                return mailboxDao.checkDiaryIdx(typeIdx);

            } else if (type.compareTo("letter") == 0) {
                return mailboxDao.checkLetterIdx(typeIdx);

            } else {
                return mailboxDao.checkReplyIdx(typeIdx);
            }

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================

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
     * [GET] /mailboxes/mail/:userIdx?type=&typeIdx=
     * userIdx = 해당 우편을 조회하는 회원 식별자
     * type = 일기, 편지, 답장 구분 (diary / letter / reply)
     * typeIdx = 식별자 정보 (type-typeIdx : diary-diaryIdx / letter-letterIdx / reply-replyIdx)
     */
    public GetMailRes getMail(int userIdx, String type, int typeIdx) throws BaseException {
        try {
            // 우편 내용 저장
            GetMailRes mail = new GetMailRes();
            mail.setFirstHistoryType(type);

            if (type.compareTo("diary") == 0) {
                mail.setMail(diaryProvider.getDiary(userIdx, typeIdx)); // 일기 정보 저장

            } else if (type.compareTo("letter") == 0) {
                mail.setMail(letterProvider.getLetter(userIdx, typeIdx)); // 편지 정보 저장

            } else {
                mail.setFirstHistoryType(mailboxDao.getFirstHistoryType(typeIdx));
                mail.setMail(replyProvider.getReply(typeIdx)); // 답장 정보 저장
            }

            // 발신인 정보 (User.nickName, User.fontIdx) 저장
            int senderFontIdx = 0;
            String senderNickName = "";

            if (type.compareTo("diary") == 0) { // 일기
                senderNickName = mailboxDao.getSenderNickName_diary(typeIdx);
                senderFontIdx = mailboxDao.getFontIdx_diary(typeIdx);

            } else if (type.compareTo("letter") == 0) { // 편지
                senderNickName = mailboxDao.getSenderNickName_letter(typeIdx);
                senderFontIdx = mailboxDao.getFontIdx_letter(typeIdx);

            } else { // 답장
                senderNickName = mailboxDao.getSenderNickName_reply(typeIdx);
                senderFontIdx = mailboxDao.getFontIdx_reply(typeIdx);
            }

            mail.setSenderNickName(senderNickName);
            mail.setSenderFontIdx(senderFontIdx);

            return mail;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
