package com.umc.btos.src.diary;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.diary.model.*;
import com.umc.btos.utils.AES128;
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
            if (postDiaryReq.getIsPublic() == 0) { // isPublic == 0(private)인 경우 -> Diary.content & Done.content 부분 암호화하여 저장
                // Diary.content 암호화
                String diaryContent = new AES128(Secret.PASSWORD_KEY).encrypt(postDiaryReq.getDiaryContent());
                postDiaryReq.setDiaryContent(diaryContent); // diaryContent 필드값 변경

                // Done.content 암호화
                List doneList = postDiaryReq.getDoneList();
                List doneList_encrypted = new ArrayList(); // 암호화된 done list 저장하는 리스트
                for (int i = 0; i < doneList.size(); i++) {
                    doneList_encrypted.add(new AES128(Secret.PASSWORD_KEY).encrypt(doneList.get(i).toString()));
                }
                postDiaryReq.setDoneList(doneList_encrypted); // doneList 필드값 변경

            }
        } catch (Exception ignored) {
            throw new BaseException(DIARY_ENCRYPTION_ERROR); // 일기 또는 done list 내용 암호화에 실패하였습니다.
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
