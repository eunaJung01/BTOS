package com.umc.btos.src.diary;

import com.umc.btos.config.BaseException;
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
     * [GET] /btos/diary/:date
     */
    public GetCheckDiaryRes checkDiary(int userIdx, String date) throws BaseException {
        try {
            return new GetCheckDiaryRes(diaryDao.checkDiary(userIdx, date));

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * Archive 조회 - 캘린더
     * [GET] /btos/diary/calendar?userIdx=&date=&type
     * date = YYYY-MM
     * type (조회 방식) = 1. doneList : 나뭇잎 색으로 done list 개수 표현 / 2. emotion : 감정 이모티콘
     */
    public List<GetCalendarRes> getCalendar(int userIdx, String date, String type) throws BaseException {
        // TODO : 형식적 validaion - 프리미엄 미가입자는 감정 이모티콘으로 조회 불가
        if (type.compareTo("emotion") == 0 && diaryDao.isPremium(userIdx).compareTo("free") == 0) {
            throw new BaseException(DIARY_NONPREMIUM_USER); // 프리미엄 가입이 필요합니다.
        }

        try {
            List<GetCalendarRes> calendar = diaryDao.getCalendarList(userIdx, date); // 캘린더 (날짜별 일기 정보 목록)

            if (type.compareTo("doneList") == 0) { // done list로 조회 -> 일기 별 doneList 개수 정보 저장 (set doneListNum)
                for (GetCalendarRes dateInfo : calendar) {
                    dateInfo.setDoneListNum(diaryDao.setDoneListNum(userIdx, dateInfo.getDiaryDate()));
                }
            } else { // emotion으로 조회 -> 일기 별 감정 이모티콘 정보 저장 (set emotionIdx)
                for (GetCalendarRes dateInfo : calendar) {
                    dateInfo.setEmotionIdx(diaryDao.setEmotion(userIdx, dateInfo.getDiaryDate()));
                }
            }
            return calendar;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * Archive 조회 - 달별 일기 리스트
     * [GET] /btos/diarylist?userIdx=&date=
     * date = YYYY-MM
     */
    public List<GetDiaryRes> getDiaryList(int userIdx, String date) throws BaseException {
        try {
            List<GetDiaryRes> diaryList = diaryDao.getDiaryList(userIdx, date); // 달별 일기 정보 저장

            // 각 일기에 해당하는 done list 정보 저장
            for (GetDiaryRes diary : diaryList) {
                int diaryIdx = diary.getDiaryIdx();
                diary.setDoneList(diaryDao.getDoneList(diaryIdx));
            }

            // content 복호화
            for (GetDiaryRes diary : diaryList) {
                if (diary.getIsPublic() == 0) { // private일 경우 (isPublic == 0)
                    // Diary.content
                    String diaryContent = diary.getContent();
                    diary.setContent(new AES128(Secret.PASSWORD_KEY).decrypt(diaryContent));

                    // Done.content
                    List<GetDoneRes> doneList = diary.getDoneList();
                    for (int j = 0; j < doneList.size(); j++) {
                        String doneContent = diary.getDoneList().get(j).getContent();
                        diary.getDoneList().get(j).setContent(new AES128(Secret.PASSWORD_KEY).decrypt(doneContent));
                    }
                }
            }
            return diaryList;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
