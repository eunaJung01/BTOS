package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.DiaryProvider;
import com.umc.btos.src.diary.DiaryDao;
import com.umc.btos.src.diary.model.GetDiaryRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * 우편함 - 일기 / 편지 / 답장 조회
     * [GET] /mailboxes/mail?type=&idx=
     * type = 일기, 편지, 답장 구분 (diary / letter / reply)
     * idx = 식별자 정보 (type-idx : diary-diaryIdx / letter-letterIdx / reply-replyIdx)
     */
    public Object setMailContent(String type, int idx) throws BaseException {
        try {
            Object mail;
            if (type.compareTo("diary") == 0) {
                mail = diaryDao.getDiary(idx); // 일기 정보 저장
                ((GetDiaryRes) mail).setDoneList(diaryDao.getDoneList(idx)); // done list 정보 저장

                // content 복호화
                if (((GetDiaryRes) mail).getIsPublic() == 0) { // private일 경우 (isPublic == 0)
                    diaryProvider.decryptContents((GetDiaryRes) mail, true);
                }

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
