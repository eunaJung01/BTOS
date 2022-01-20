package com.umc.btos.src.mailbox;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.*;
import com.umc.btos.src.mailbox.model.GetDiaryRes_Mailbox;
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
     * 우편함 - 일기 조회
     * [GET] /btos/mailbox?diaryIdx=
     */
    public GetDiaryRes_Mailbox getDiary(int diaryIdx) throws BaseException {
        try {
            GetDiaryRes_Mailbox diary_mailbox = new GetDiaryRes_Mailbox(diaryDao.getDiary(diaryIdx)); // 일기 정보 저장 (GetDiaryRes)
            diary_mailbox.getDiary().setDoneList(diaryDao.getDoneList(diaryIdx)); // done list 정보 저장

            // content 복호화
            if (diary_mailbox.getDiary().getIsPublic() == 0) { // private일 경우 (isPublic == 0)
                diaryProvider.decryptContents(diary_mailbox.getDiary());
            }

            diary_mailbox.setSenderFontIdx(mailboxDao.getFontIdx(diaryIdx)); // 발송자의 폰트 정보 저장
            return diary_mailbox;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
