package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.diary.model.*;
import com.umc.btos.utils.AES128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
     * Archive 조회 - 달력
     * [GET] /diaries/calendar/:userIdx/:date?type
     * date = YYYY.MM
     * type (조회 방식) = 1. doneList : 나뭇잎 색으로 done list 개수 표현 / 2. emotion : 감정 이모티콘
     */
    public List<GetCalendarRes> getCalendar(int userIdx, String date, String type) throws BaseException {
        // TODO : 의미적 validation - 프리미엄 미가입자는 감정 이모티콘으로 조회 불가
        if (type.compareTo("emotion") == 0 && diaryDao.isPremium(userIdx).compareTo("free") == 0) {
            throw new BaseException(DIARY_NONPREMIUM_USER); // 프리미엄 가입이 필요합니다.
        }

        try {
            // 달력 : 한달 단위로 날짜마다 저장된 일기에 대한 정보(done list 개수 또는 감정 이모티콘 식별자)를 저장
            List<GetCalendarRes> calendar = diaryDao.getCalendarList(userIdx, date);

            if (type.compareTo("doneList") == 0) { // done list로 조회 -> 일기 별 doneList 개수 저장 (set doneListNum)
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
     * Archive 조회 - 일기 리스트
     * [GET] /diaries/diaryList/:userIdx/:pageNum?search=&startDate=&endDate=
     * search = 검색할 문자열 ("String")
     * startDate, lastDate = 날짜 기간 설정 (YYYY.MM.DD ~ YYYY.MM.DD)
     * 검색 & 기간 설정 조회는 중첩됨
     * 최신순 정렬 (diaryDate 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     *
     * 1. 전체 조회 - default
     * 2. 문자열 검색 (search)
     * 3. 기간 설정 조회 (startDate ~ endDate)
     * 4. 문자열 검색 & 기간 설정 조회 (search, startDate ~ endDate)
     */
    public List<GetDiaryRes> getDiaryList(String[] params, PagingRes pageInfo) throws BaseException, NullPointerException {
        try {
            // String[] params = new String[]{userIdx, search, startDate, lastDate};
            int userIdx = Integer.parseInt(params[0]);
            String search = params[1];
            String startDate = params[2];
            String endDate = params[3];

            // PagingRes
            int pageNum = pageInfo.getCurrentPage(); // 페이지 번호
            double dataNum = 0; // data 총 개수 (후에 Math.ceil 사용하는 연산 때문에 double)
            boolean needsPaging = false;

            List<GetDiaryRes> diaryList = new ArrayList<>(); // 일기 정보 저장 (done list 조회 X, 일기 내용만 조회)

            // 1. 전체 조회 - default
            if (search == null && startDate == null && endDate == null) {
                diaryList = diaryDao.getDiaryList(userIdx, pageNum);
                dataNum = diaryDao.getDiaryList_dataNum(userIdx);
            }

            // 2. 문자열 검색 (search)
            // search & Diary.content : 띄어쓰기 모두 제거 -> 찾기
            else if (search != null && startDate == null && endDate == null) {
                search = search.replaceAll("\"", ""); // 따옴표 제거
                search = search.replaceAll(" ", ""); // 공백 제거

                List<Integer> diaryIdxList = diaryDao.getDiaryIdxList(userIdx); // 특정 회원의 모든 일기 diaryIdx : List 형태로 저장

                for (int diaryIdx : diaryIdxList) { // 차례대로 문자열 검색 -> 없다면 diaryIdxList에서 제거
                    String diaryContent = diaryDao.getDiaryContent(diaryIdx);
                    if (diaryDao.getIsPublic(diaryIdx) == 0) { // private 일기일 경우 content 복호화
                        diaryContent = new AES128(Secret.PASSWORD_KEY).decrypt(diaryContent);
                    }

                    if (searchString(diaryContent, search)) { // 문자열 검색 -> 찾는 값이 존재하는 일기들만 저장
                        diaryList.add(diaryDao.getDiary(diaryIdx));
                    }
                }
                dataNum = diaryList.size();
                if (dataNum > Constant.DIARYLIST_DATA_NUM) { // 페이징 처리 필요
                    needsPaging = true;
                }

            } else {
                // 3. 기간 설정 조회 (startDate ~ endDate)
                diaryList = diaryDao.getDiaryListByDate(userIdx, startDate, endDate, pageNum);
                dataNum = diaryDao.getDiaryListByDate_dataNum(userIdx, startDate, endDate);

                // 4. 문자열 검색 & 날짜 기간 설정 조회 (search, startDate ~ endDate)
                if (search != null) {
                    search = search.replaceAll("\"", ""); // 따옴표 제거
                    search = search.replaceAll(" ", ""); // 공백 제거

                    for (int i = 0; i < diaryList.size(); i++) {
                        String diaryContent = diaryList.get(i).getContent();
                        if (diaryList.get(i).getIsPublic() == 0) { // private 일기일 경우 content 복호화
                            diaryContent = new AES128(Secret.PASSWORD_KEY).decrypt(diaryContent);
                        }

                        if (!searchString(diaryContent, search)) { // 문자열 검색 -> 찾는 값이 존재하지 않는 일기들은 제거
                            diaryList.remove(i);
                        }
                    }
                    dataNum = diaryList.size();
                    if (dataNum > Constant.DIARYLIST_DATA_NUM) { // 페이징 처리 필요
                        needsPaging = true;
                    }
                }
            }

            if (dataNum == 0) {
                throw new NullPointerException(); // 검색 결과 없음
            }

            // PagingRes
            int endPage = (int) Math.ceil(dataNum / Constant.DIARYLIST_DATA_NUM); // 마지막 페이지 번호
            if (pageInfo.getCurrentPage() > endPage) {
                throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
            }
            pageInfo.setEndPage(endPage);
            pageInfo.setHasNext(pageInfo.getCurrentPage() != endPage); // pageNum == endPage -> hasNext = false

            // 페이징 처리
            if (needsPaging) {
                int startDataIdx = (pageNum - 1) * Constant.DIARYLIST_DATA_NUM;
                int endDataIdx = pageNum * Constant.DIARYLIST_DATA_NUM;

                List<GetDiaryRes> diaryList_paging = new ArrayList<>(); // 일기 정보 저장 (done list 조회 X, 일기 내용만 조회)
                for (int i = startDataIdx; i < endDataIdx; i++) {
                    diaryList_paging.add(diaryList.get(i));
                }
                diaryList = diaryList_paging;
            }

            // content 복호화
            for (GetDiaryRes diary : diaryList) {
                if (diary.getIsPublic() == 0) { // private 일기일 경우 content 복호화
                    decryptContents(diary, false);
                }
            }

            return diaryList;

        } catch (NullPointerException exception) {
            throw new BaseException(EMPTY_RESULT); // 검색 결과 없음
        } catch (BaseException exception) {
            throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 문자열 검색 (in Diary.content)
    public boolean searchString(String diaryContent, String search) {
        String content_spaceDeleted = diaryContent.replaceAll(" ", ""); // 공백 제거
        return content_spaceDeleted.contains(search); // 문자열 검색 (존재 : true, 미존재 : false)
    }

    // content 복호화
    public void decryptContents(GetDiaryRes diary, boolean hasDoneList) throws BaseException {
        try {
            // Diary.content
            String diaryContent = diary.getContent();
            diary.setContent(new AES128(Secret.PASSWORD_KEY).decrypt(diaryContent));

            // Done.content
            if (hasDoneList) {
                List<GetDoneRes> doneList = diary.getDoneList();
                for (int j = 0; j < doneList.size(); j++) {
                    String doneContent = diary.getDoneList().get(j).getContent();
                    diary.getDoneList().get(j).setContent(new AES128(Secret.PASSWORD_KEY).decrypt(doneContent));
                }
            }

        } catch (Exception ignored) {
            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
        }
    }

    /*
     * 일기 조회
     * [GET] /diaries/:diaryIdx
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

}
