package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.diary.model.*;
import com.umc.btos.utils.AES128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class DiaryProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DiaryDao diaryDao;

    @Autowired
    public DiaryProvider(DiaryDao diaryDao) {
        this.diaryDao = diaryDao;
    }

    /*
     * 일기 작성 여부 확인
     * [GET] /diaries/:date
     */
    public GetCheckDiaryRes checkDiaryDate(int userIdx, String date) throws BaseException {
        try {
            return new GetCheckDiaryRes(diaryDao.checkDiaryDate(userIdx, date));

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 일기 조회
     */
    public GetDiaryRes getDiary(int diaryIdx) throws BaseException {
        try {
            GetDiaryRes diary = diaryDao.getDiary(diaryIdx); // 일기의 정보
            diary.setDoneList(diaryDao.getDoneList(diaryIdx)); // done list 정보

            // content 복호화
            if (diary.getIsPublic() == 0) { // private 일기일 경우 content 복호화
                decryptContents(diary, true);
            }
            return diary;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // content 복호화
    public void decryptContents(GetDiaryRes diary, boolean hasDoneList) throws BaseException {
        try {
            // Diary.content
            String diaryContent = diary.getContent();
            diary.setContent(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(diaryContent));

            // Done.content
            if (hasDoneList) {
                List<GetDoneRes> doneList = diary.getDoneList();
                for (int j = 0; j < doneList.size(); j++) {
                    String doneContent = diary.getDoneList().get(j).getContent();
                    diary.getDoneList().get(j).setContent(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(doneContent));
                }
            }

        } catch (Exception ignored) {
            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
        }
    }

}
