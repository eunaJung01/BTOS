package com.umc.btos.src.archive;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.*;
import com.umc.btos.src.archive.model.*;
import com.umc.btos.utils.AES128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class ArchiveProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArchiveDao archiveDao;

    @Autowired
    public ArchiveProvider(ArchiveDao archiveDao) {
        this.archiveDao = archiveDao;
    }

//    @Value("${secret.private-diary-key}")
//    String PRIVATE_DIARY_KEY;

    // ================================================== validation ==================================================

    /*
     * 존재하는 회원인지 확인
     */
    public int checkUserIdx(int userIdx) throws BaseException {
        try {
            return archiveDao.checkUserIdx(userIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 존재하는 일기인지 확인
     */
    public int checkDiaryIdx(int diaryIdx) throws BaseException {
        try {
            return archiveDao.checkDiaryIdx(diaryIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================================================

    /*
     * 달력 조회
     * [GET] /archives/calendar/:userIdx/:date?type=
     * date = yyyy.MM
     * type (조회 방식) = 1. doneList : 나뭇잎 색으로 done list 개수 표현 / 2. emotion : 감정 이모티콘
     */
    public List<GetCalendarRes> getCalendar(int userIdx, String date, String type) throws BaseException {
        // TODO : 의미적 validation - 프리미엄 미가입자는 감정 이모티콘으로 조회 불가
//        if (type.compareTo("emotion") == 0 && archiveDao.isPremium(userIdx).compareTo("free") == 0) {
//            throw new BaseException(DIARY_NONPREMIUM_USER); // 프리미엄 가입이 필요합니다.
//        }

        try {
            // 달력 : 한달 단위로 날짜마다 저장된 일기에 대한 정보(done list 개수 또는 감정 이모티콘 식별자)를 저장
            List<GetCalendarRes> calendar = archiveDao.getCalendarList(userIdx, date);

            // 1. type =  done list -> 일기 별 doneList 개수 저장 (set doneListNum)
            if (type.compareTo("doneList") == 0) {
                for (GetCalendarRes dateInfo : calendar) {
                    dateInfo.setDoneListNum(archiveDao.setDoneListNum(userIdx, dateInfo.getDiaryDate()));
                }
            }
            // 2. type = emotion -> 일기 별 감정 이모티콘 정보 저장 (set emotionIdx)
            else {
                for (GetCalendarRes dateInfo : calendar) {
                    dateInfo.setEmotionIdx(archiveDao.getEmotionIdx(userIdx, dateInfo.getDiaryDate()));
                }
            }
            return calendar;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 일기 리스트 조회
     * [GET] /archives/diaryList/:userIdx/:pageNum?search=&startDate=&endDate=
     * search = 검색할 문자열 ("String")
     * startDate, lastDate = 날짜 기간 설정 (yyyy.MM.dd ~ yyyy.MM.dd)
     * 검색 & 기간 설정 조회는 중첩됨
     * 검색 시 띄어쓰기, 영문 대소문자 구분없이 조회됨
     * 최신순 정렬 (diaryDate 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    public List<GetDiaryListRes> getDiaryList(String[] params, PagingRes pageInfo) throws BaseException, NullPointerException {
        try {
            // String[] params = new String[]{userIdx, search, startDate, lastDate};
            int userIdx = Integer.parseInt(params[0]);
            String search = params[1];
            String startDate = params[2];
            String endDate = params[3];

            // PagingRes
            int pageNum = pageInfo.getCurrentPage(); // 페이지 번호
            double dataNum_total = 0; // 총 데이터 개수 (후에 Math.ceil 사용하는 연산 때문에 double)

            List<GetDiaryListRes> result = new ArrayList<>();

            // diaryList
            dataNum_total = archiveDao.getDiaryListNum(userIdx, search, startDate, endDate);
            if (dataNum_total == 0) {
                throw new NullPointerException(); // 검색 결과 없음
            }

            List<Diary> diaryList = archiveDao.getDiaryList(userIdx, search, startDate, endDate, pageNum); // 일기 정보 저장 (done list 조회 X, 일기 내용만 조회)
            for (Diary diary : diaryList) { // set doneListNum
                diary.setDoneListNum(archiveDao.setDoneListNum(diary.getDiaryIdx()));
            }
            pageInfo.setDataNum_currentPage(diaryList.size()); // 현재 페이지의 데이터 개수

            // monthList
            List<String> monthList = archiveDao.getMonthList(userIdx, search, startDate, endDate, pageNum); // response에 들어가게 되는 날짜들 저장 (yyyy.MM)

            for (String month : monthList) {
                List<Diary> diaryList_month = new ArrayList<>(); // 같은 '년도-달'인 일기들을 묶는 리스트
                for (Diary diary : diaryList) {
                    if (diary.getDiaryDate().contains(month)) { //
                        diaryList_month.add(diary);
                    }
                }
                if (diaryList_month.size() != 0) {
                    result.add(new GetDiaryListRes(month, diaryList_month)); // GetDiaryListRes 객체 생성
                }
            }

            // PagingRes
            pageInfo.setDataNum_total((int) dataNum_total);
            int endPage = (int) Math.ceil(dataNum_total / Constant.DIARYLIST_DATA_NUM); // 마지막 페이지 번호
            if (endPage == 0) endPage = 1;
            if (pageInfo.getCurrentPage() > endPage) {
                throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
            }
            pageInfo.setEndPage(endPage);
            pageInfo.setHasNext(pageInfo.getCurrentPage() != endPage); // pageNum == endPage -> hasNext = false

            return result;

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
        diaryContent = diaryContent.replaceAll(" ", "").toLowerCase(); // 공백 제거, 영문 대소문자 구별 X
        return diaryContent.contains(search); // 문자열 검색 (존재 : true, 미존재 : false)
    }

    /*
     * 일기 조회
     * [GET] /archives/:diaryIdx
     */
    public GetDiaryRes getDiary(int diaryIdx) throws BaseException {
        try {
            // 일기
            Diary diary = archiveDao.getDiary(diaryIdx);
//            if (archiveDao.getIsPublic(diaryIdx) == 0) { // private 일기일 경우 Diary.content 복호화
//                decryptContents(diary);
//            }
            int isPublic = archiveDao.getIsPublic(diaryIdx);

            // done list
            List<String> doneList = new ArrayList<>();
            if (archiveDao.hasDoneList(diaryIdx)) { // 해당 일기에 done list가 있는 경우
                doneList.addAll(archiveDao.getDoneList(diaryIdx));

//                if (isPublic == 0) { // private 일기일 경우 Done.content 복호화
//                    doneList = decryptContents(doneList); // doneList 갱신
//                }
            }

            return new GetDiaryRes(diary.getDiaryIdx(), diary.getEmotionIdx(), isPublic, diary.getDiaryDate(), diary.getContent(), doneList);

//        } catch (BaseException exception) {
//            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================ content 복호화 ================================================

//    // 일기
//    public void decryptContents(Diary diary) throws BaseException {
//        try {
//            // Diary.content
//            String diaryContent = diary.getContent();
////            diary.setContent(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(diaryContent));
//            diary.setContent(new AES128(PRIVATE_DIARY_KEY).decrypt(diaryContent));
//
//        } catch (Exception ignored) {
//            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
//        }
//    }
//
//    // done list
//    public List<String> decryptContents(List<String> doneList) throws BaseException {
//        try {
//            // Done.content
//            List<String> doneList_decrypted = new ArrayList<>();
//            for (String doneContent : doneList) {
////                doneList_decrypted.add(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(doneContent));
//                doneList_decrypted.add(new AES128(PRIVATE_DIARY_KEY).decrypt(doneContent));
//            }
//            return doneList_decrypted;
//
//        } catch (Exception ignored) {
//            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
//        }
//    }

}
