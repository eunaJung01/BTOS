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

    @Autowired
    public MailboxProvider(MailboxDao mailboxDao) {
        this.mailboxDao = mailboxDao;
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

    /*
     * 해당 우편을 받은 회원(userIdx == receiverIdx)인지 확인
     */
    public int checkUserAboutMail(int userIdx, String type, int typeIdx) throws BaseException {
        try {
            if (type.compareTo("diary") == 0) {
                return mailboxDao.checkUserAboutMail_diary(userIdx, typeIdx);

            } else if (type.compareTo("letter") == 0) {
                return mailboxDao.checkUserAboutMail_letter(userIdx, typeIdx);

            } else {
                return mailboxDao.checkUserAboutMail_reply(userIdx, typeIdx);
            }

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================

    /*
     * 우편함 목록 조회
     * [GET] /mailboxes?userIdx
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
     * [GET] /mailboxes/mail?userIdx?type=&typeIdx=
     * userIdx = 해당 우편을 조회하는 회원 식별자
     * type = 일기, 편지, 답장 구분 (diary / letter / reply)
     * typeIdx = 식별자 정보 (type-typeIdx : diary-diaryIdx / letter-letterIdx / reply-replyIdx)
     */
    public GetMailRes getMail(int userIdx, String type, int typeIdx) throws BaseException {
        try {
            GetMailRes mail;

            // type = diary
            if (type.compareTo("diary") == 0) {
                mail = mailboxDao.getMail_diary(userIdx, typeIdx);
                if (mailboxDao.hasDoneList(typeIdx)) { // done list 유무 확인
                    mail.setDoneList(mailboxDao.getDoneList(typeIdx));
                }

                // 열람 여부 변경
                mailboxDao.modifyIsChecked_diary(userIdx, typeIdx); // DiarySendList.isChecked = 1로 변환

            }

            // type = letter
            else if (type.compareTo("letter") == 0) {
                mail = mailboxDao.getMail_letter(userIdx, typeIdx);

                // 열람 여부 변경
                mailboxDao.modifyIsChecked_letter(userIdx, typeIdx); // LetterSendList.isChecked = 1로 변환
            }

            // type = reply
            else {
                mail = mailboxDao.getMail_reply(userIdx, typeIdx);

                // 열람 여부 변경
                mailboxDao.modifyIsChecked_reply(userIdx, typeIdx); // Reply.isChecked = 1로 변환
            }

            // set senderActive
            mail.setSenderActive(mailboxDao.getSenderActive(type, typeIdx)); // User.status 확인

            return mail;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
