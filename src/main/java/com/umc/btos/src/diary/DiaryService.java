package com.umc.btos.src.diary;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class DiaryService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DiaryDao diaryDao;

    @Autowired
    public DiaryService(DiaryDao diaryDao) {
        this.diaryDao = diaryDao;
    }

    // 일기 저장 (POST)
    public PostDiaryRes saveDiary(PostDiaryReq postDiaryReq) throws BaseException {
        // TODO : 형식적 validation - 당일에 작성한 일기가 아니라면 발송 불가
        LocalDate now = LocalDate.now(); // 오늘 날짜 (YYYY-MM-DD)

        if (postDiaryReq.getDiaryDate().compareTo(now.toString()) != 0 && postDiaryReq.getIsPublic() == 1) { // 작성일 != 일기의 해당 날짜일 경우 발송(public으로 지정) 불가
            throw new BaseException(UNPRIVATE_DATE); // 당일에 작성한 일기만 발송 가능합니다!
        }

        try {
            int diaryIdx = diaryDao.saveDiary(postDiaryReq);
            List doneIdxList = diaryDao.saveDoneList(diaryIdx, postDiaryReq.getDoneList());
            return new PostDiaryRes(diaryIdx, doneIdxList);

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
