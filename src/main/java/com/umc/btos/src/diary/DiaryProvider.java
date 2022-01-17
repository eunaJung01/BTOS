package com.umc.btos.src.diary;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.model.GetCheckDiaryRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;

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
     * [GET] /btos/diary/:date
     */
    public GetCheckDiaryRes checkDiary(int userIdx, String date) throws BaseException {
        try {
            return new GetCheckDiaryRes(diaryDao.checkDiary(userIdx, date));

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
