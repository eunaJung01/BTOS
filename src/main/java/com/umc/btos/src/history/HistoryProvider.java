package com.umc.btos.src.history;

import com.umc.btos.config.*;
import com.umc.btos.src.history.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class HistoryProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HistoryDao historyDao;

    @Autowired
    public HistoryProvider(HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    /*
     * History 목록 조회
     * [GET] /histories/list/:userIdx/:pageNum?filtering=&search=
     * filtering = 1. sender : 발신인 / 2. diary : 일기만 / 3. letter : 편지만
     * search = 검색할 문자열 ("String")
     * 최신순 정렬 (createdAt 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    public GetHistoryListRes getHistoryList(String[] params, PagingRes pageInfo) throws BaseException, NullPointerException {
        try {
            // String[] params = new String[]{userIdx, filtering, search};
            int userIdx = Integer.parseInt(params[0]);
            String filtering = params[1];
            String search = params[2];

            // PagingRes
            int pageNum = pageInfo.getCurrentPage(); // 페이지 번호
            double dataNum = 0; // data 총 개수 (후에 Math.ceil 사용하는 연산 때문에 double)

            GetHistoryListRes historyListRes = new GetHistoryListRes(filtering);
            /*
             * filtering = "sender" && search == null인 경우 GetHistoryListRes.list = List<HistoryList_Sender>
             * filtering = "sender" && search != null인 경우 GetHistoryListRes.list = List<History>
             * filtering = "diary" 또는 "letter"인 경우 GetHistoryListRes.list = List<History>
             * History_Sender, History 객체 : 수신한 일기 또는 편지에 대한 상세 정보를 저장
             */

            if (search == null) {
                /*
                 * 문자열 검색 X
                 *
                 * 1. filtering = sender(발신인)
                 *      senderNickNameList에 저장되어 있는 닉네임 순서대로(createdAt 기준 내림차순 정렬) HistoryList_Sender 객체 생성
                 *      HistoryList_Sender 필드 : senderNickName(발신자 닉네임), historyListNum(History 개수), History firstContent
                 *      -> HistoryList_Sender 객체들을 List 형태로 묶어서 GetHistoryListRes.list에 저장
                 *
                 * 2. filtering = diary(일기만) & letter(편지만)
                 *      History 객체들을 List 형태로 묶어서 GetHistoryListRes.list에 저장
                 */

                // 발신인
                if (filtering.compareTo("sender") == 0) {
                    List<HistoryList_Sender> historyListRes_list = new ArrayList<>(); // GetHistoryListRes.list

                    // userIdx 회원이 받은 일기와 편지의 발신자 닉네임 목록 (createdAt 기준 내림차순 정렬)
                    List<String> senderNickNameList = historyDao.getNickNameList_sortedByCreatedAt(userIdx);

                    // 페이징 처리
                    dataNum = senderNickNameList.size();
                    if (dataNum > Constant.HISTORY_DATA_NUM) {
                        int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                        int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;

                        List<String> senderNickNameList_paging = new ArrayList<>();
                        for (int i = startDataIdx; i < endDataIdx; i++) {
                            senderNickNameList_paging.add(senderNickNameList.get(i));
                        }
                        senderNickNameList = senderNickNameList_paging;
                    }

                    for (String senderNickName : senderNickNameList) {
                        List<History_Sender> historyList = new ArrayList<>(); // HistoryList_Sender.historyList

                        if (historyDao.hasHistory_diary(userIdx, senderNickName) != 0) { // null 확인
                            historyList.add(historyDao.getDiaryList_sender(userIdx, senderNickName)); // 일기
                        }
                        if (historyDao.hasHistory_letter(userIdx, senderNickName) != 0) { // null 확인
                            historyList.add(historyDao.getLetterList_sender(userIdx, senderNickName)); // 편지
                        }
                        Collections.sort(historyList); // createAt 기준 내림차순 정렬

                        int historyListNum = historyDao.getDiaryListSize_sender(userIdx, senderNickName) + historyDao.getLetterListSize_sender(userIdx, senderNickName);

                        HistoryList_Sender historyList_sender = new HistoryList_Sender(senderNickName, historyListNum, historyList.get(0));
                        historyListRes_list.add(historyList_sender);
                    }

                    if (historyListRes_list.size() != 0) {
                        historyListRes.setList(historyListRes_list);
                    } else {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                }

                // 일기만
                else if (filtering.compareTo("diary") == 0) {
                    List<History> historyList = new ArrayList<>(); // GetHistoryListRes.list

                    if (historyDao.hasHistory_diary(userIdx) != 0) { // null 확인
                        historyList.addAll(historyDao.getDiaryList(userIdx, pageNum)); // 일기 (createAt 기준 내림차순 정렬)
                        dataNum = historyDao.getDiaryList_dataNum(userIdx);

                    } else {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                    historyListRes.setList(historyList);
                }

                // 편지만
                else {
                    List<History> historyList = new ArrayList<>(); // GetHistoryListRes.list

                    if (historyDao.hasHistory_letter(userIdx) != 0) { // null 확인
                        historyList.addAll(historyDao.getLetterList(userIdx, pageNum)); // 편지 (createAt 기준 내림차순 정렬)
                        dataNum = historyDao.getLetterList_dataNum(userIdx);

                    } else {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                    historyListRes.setList(historyList);
                }

            } else {
                /*
                 * 문자열 검색 (search)
                 * search && (Diary.content || Letter.content) : 띄어쓰기 모두 제거 후 찾기
                 *
                 * 1. filtering = sender(발신인)
                 *      senderNickNameList에 저장되어 있는 닉네임 순서대로(createdAt 기준 내림차순 정렬)
                 *      userIdx 회원이 수신한 일기 또는 편지에서 가장 최근에 받은 일기와 편지에 대한 diaryIdx, letterIdx 받기
                 *      둘 중 더 최근에 받은 데이터의 content에서 문자열 검색
                 *      찾는 문자열이 있는 idx의 정보들만 List<HistoryList_Sender>에 저장
                 *      -> GetSenderRes.historyList에 저장
                 *
                 * 2. filtering = diary(일기만) & letter(편지만)
                 *      userIdx 회원이 수신한 일기 또는 편지에 대한 diaryIdxList, letterIdxList 생성
                 *      list에 저장되어 있는 값을 통해 차례대로 content를 불러와서 문자열 검색 -> 찾는 문자열이 있는 idx들만 따로 저장하여 list 갱신
                 *      list 정보를 통해서 List<History>에 add
                 *      -> createdAt 기준 내림차순 정렬, GetSenderRes.historyList에 저장
                 */

                search = search.replaceAll("\"", ""); // 따옴표 제거
                search = search.replaceAll(" ", ""); // 공백 제거

                // 발신인
                if (filtering.compareTo("sender") == 0) {
                    List<HistoryList_Sender> historyListRes_list = new ArrayList<>(); // GetHistoryListRes.list

                    // userIdx 회원이 받은 일기와 편지의 발신자 닉네임 목록 (createdAt 기준 내림차순 정렬)
                    List<String> senderNickNameList = historyDao.getNickNameList_sortedByCreatedAt(userIdx);

                    for (String senderNickName : senderNickNameList) {
                        int diaryIdx = 0;
                        int letterIdx = 0;

                        // diaryIdxList
                        if (historyDao.hasHistory_diary(userIdx, senderNickName) != 0) { // null 확인
                            diaryIdx = historyDao.getDiaryIdxList_sender(userIdx, senderNickName); // 수신받은 일기 diaryIdx

                            String diaryContent = historyDao.getDiaryContent(diaryIdx);

                            if (!searchString(diaryContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                                diaryIdx = 0;
                            }
                        }
                        // letterIdxList
                        if (historyDao.hasHistory_letter(userIdx, senderNickName) != 0) { // null 확인
                            letterIdx = historyDao.getLetterIdxList_sender(userIdx, senderNickName); // 수신받은 편지 letterIdx

                            String letterContent = historyDao.getLetterContent(letterIdx);

                            if (!searchString(letterContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                                letterIdx = 0;
                            }
                        }

                        List<History_Sender> historyList = new ArrayList<>(); // HistoryList_Sender.historyList
                        if (diaryIdx != 0) {
                            historyList.add(historyDao.getDiary_sender(userIdx, diaryIdx));
                        }
                        if (letterIdx != 0) {
                            historyList.add(historyDao.getLetter_sender(userIdx, letterIdx));
                        }
                        if (historyList.size() != 0) {
                            Collections.sort(historyList); // createAt 기준 내림차순 정렬
                            historyListRes_list.add(new HistoryList_Sender(senderNickName, historyList.size(), historyList.get(0)));
                        }
                    }

                    // 페이징 처리 - 발신인 명수 : HISTORY_DATA_NUM
                    dataNum = historyListRes_list.size();
                    if (dataNum > Constant.HISTORY_DATA_NUM) {
                        int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                        int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;

                        List<HistoryList_Sender> historyListRes_list_paging = new ArrayList<>();
                        for (int i = startDataIdx; i < endDataIdx; i++) {
                            historyListRes_list_paging.add(historyListRes_list.get(i));
                        }
                        historyListRes_list = historyListRes_list_paging;
                    }

                    if (historyListRes_list.size() != 0) {
                        historyListRes.setList(historyListRes_list);
                    } else {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                }

                // 일기만
                else if (filtering.compareTo("diary") == 0) {
                    List<Integer> diaryIdxList = new ArrayList<>();

                    if (historyDao.hasHistory_diary(userIdx) != 0) { // null 확인
                        diaryIdxList.addAll(historyDao.getDiaryIdxList(userIdx, pageNum)); // 수신받은 모든 일기 diaryIdx
                        dataNum = historyDao.getDiaryIdxList_dataNum(userIdx);

                        List<Integer> diaryIdxList_searched = new ArrayList<>();
                        for (int diaryIdx : diaryIdxList) {
                            String diaryContent = historyDao.getDiaryContent(diaryIdx);

                            if (searchString(diaryContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                                diaryIdxList_searched.add(diaryIdx);
                            }
                        }
                        diaryIdxList = diaryIdxList_searched; // diaryIdxList 갱신
                    }

                    if (diaryIdxList.size() != 0) {
                        List<History> historyList = new ArrayList<>(); // HistoryList_Sender.historyList

                        for (int diaryIdx : diaryIdxList) {
                            historyList.add(historyDao.getDiary(userIdx, diaryIdx));
                        }
                        Collections.sort(historyList); // createAt 기준 내림차순 정렬
                        historyListRes.setList(historyList);

                    } else {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                }

                // 편지만
                else {
                    List<Integer> letterIdxList = new ArrayList<>();

                    if (historyDao.hasHistory_letter(userIdx) != 0) { // null 확인
                        letterIdxList.addAll(historyDao.getLetterIdxList(userIdx, pageNum)); // 수신받은 모든 편지 letterIdx
                        dataNum = historyDao.getLetterIdxList_dataNum(userIdx);

                        List<Integer> letterIdxList_searched = new ArrayList<>();
                        for (int letterIdx : letterIdxList) {
                            String letterContent = historyDao.getLetterContent(letterIdx);

                            if (searchString(letterContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                                letterIdxList_searched.add(letterIdx);
                            }
                        }
                        letterIdxList = letterIdxList_searched; // letterIdxList 갱신
                    }

                    if (letterIdxList.size() != 0) {
                        List<History> historyList = new ArrayList<>(); // HistoryList_Sender.historyList

                        for (int letterIdx : letterIdxList) {
                            historyList.add(historyDao.getLetter(userIdx, letterIdx));
                        }
                        Collections.sort(historyList); // createAt 기준 내림차순 정렬
                        historyListRes.setList(historyList);

                    } else {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                }
            }

            // PagingRes
            int endPage = (int) Math.ceil(dataNum / Constant.HISTORY_DATA_NUM); // 마지막 페이지 번호
            if (pageInfo.getCurrentPage() > endPage) {
                throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
            }
            pageInfo.setEndPage(endPage);
            pageInfo.setHasNext(pageInfo.getCurrentPage() != endPage); // pageNum == endPage -> hasNext = false

            return historyListRes;

        } catch (NullPointerException nullPointerException) {
            throw new BaseException(EMPTY_RESULT); // 검색 결과 없음
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 문자열 검색
    public boolean searchString(String content, String search) {
        String content_spaceDeleted = content.replaceAll(" ", ""); // 공백 제거
        return content_spaceDeleted.contains(search); // 문자열 검색 (존재 : true, 미존재 : false)
    }

    /*
     * History 발신인 조회
     * [GET] /histories/sender/:userIdx/:senderNickName/:pageNum?search=
     * search = 검색할 문자열 ("String")
     * 최신순 정렬 (createdAt 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    public GetSenderRes getHistoryList_sender(String[] params, PagingRes pageInfo) throws BaseException {
        try {
            // String[] params = new String[]{userIdx, senderNickName, search};
            int userIdx = Integer.parseInt(params[0]);
            String senderNickName = params[1];
            String search = params[2];

            // PagingRes
            int pageNum = pageInfo.getCurrentPage(); // 페이지 번호
            double dataNum = 0; // data 총 개수 (후에 Math.ceil 사용하는 연산 때문에 double)

            GetSenderRes historyList_sender = new GetSenderRes(senderNickName);
            List<History> historyList = new ArrayList<>(); // GetSenderRes.historyList

            if (search == null) {
                if (historyDao.hasHistory_diary(userIdx, senderNickName) != 0) { // null 확인
                    historyList.addAll(historyDao.getDiaryList(userIdx, senderNickName)); // 일기
                }
                if (historyDao.hasHistory_letter(userIdx, senderNickName) != 0) { // null 확인
                    historyList.addAll(historyDao.getLetterList(userIdx, senderNickName)); // 편지
                }
                Collections.sort(historyList); // createAt 기준 내림차순 정렬

                if (historyList.size() != 0) {
                    dataNum = historyList.size();

                    // 페이징 처리
                    if (dataNum > Constant.HISTORY_DATA_NUM) {
                        int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                        int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;

                        List<History> historyList_paging = new ArrayList<>();
                        for (int i = startDataIdx; i < endDataIdx; i++) {
                            historyList_paging.add(historyList.get(i));
                        }
                        historyList = historyList_paging;
                    }
                    historyList_sender.setHistoryList(historyList);

                } else {
                    throw new NullPointerException(); // 검색 결과 없음
                }

            } else {
                /*
                 * 문자열 검색 (search)
                 * search && (Diary.content || Letter.content) : 띄어쓰기 모두 제거 후 찾기
                 *
                 * filtering = sender(발신인)
                 *      수신받은 모든 일기와 편지들의 idx를 저장하는 list 각각 생성 (diaryIdxList, letterIdxList)
                 *      각 list에 저장되어 있는 값을 통해 차례대로 content를 불러와서 문자열 검색 -> 찾는 문자열이 있는 idx들만 따로 저장하여 list 갱신
                 *      list 정보를 통해서 List<History>에 add
                 *      -> createdAt 기준 내림차순 정렬, GetSenderRes.historyList에 저장
                 */

                search = search.replaceAll("\"", ""); // 따옴표 제거
                search = search.replaceAll(" ", ""); // 공백 제거

                List<Integer> diaryIdxList = new ArrayList<>();
                List<Integer> letterIdxList = new ArrayList<>();

                // diaryIdxList
                if (historyDao.hasHistory_diary(userIdx, senderNickName) != 0) { // null 확인
                    diaryIdxList.addAll(historyDao.getDiaryIdxList(userIdx, senderNickName)); // 수신받은 모든 일기 diaryIdx

                    List<Integer> diaryIdxList_searched = new ArrayList<>(); // 임시 list
                    for (int diaryIdx : diaryIdxList) {
                        String diaryContent = historyDao.getDiaryContent(diaryIdx);

                        if (searchString(diaryContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                            diaryIdxList_searched.add(diaryIdx);
                        }
                    }
                    diaryIdxList = diaryIdxList_searched; // diaryIdxList 갱신
                }

                // letterIdxList
                if (historyDao.hasHistory_letter(userIdx, senderNickName) != 0) { // null 확인
                    letterIdxList.addAll(historyDao.getLetterIdxList(userIdx, senderNickName)); // 수신받은 모든 편지 letterIdx

                    List<Integer> letterIdxList_searched = new ArrayList<>(); // 임시 list
                    for (int letterIdx : letterIdxList) {
                        String letterContent = historyDao.getLetterContent(letterIdx);

                        if (searchString(letterContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                            letterIdxList_searched.add(letterIdx);
                        }
                    }
                    letterIdxList = letterIdxList_searched; // letterIdxList 갱신
                }

                if (diaryIdxList.size() != 0 || letterIdxList.size() != 0) {
                    for (int diaryIdx : diaryIdxList) {
                        historyList.add(historyDao.getDiary(userIdx, diaryIdx));
                    }
                    for (int letterIdx : letterIdxList) {
                        historyList.add(historyDao.getLetter(userIdx, letterIdx));
                    }
                    Collections.sort(historyList); // createAt 기준 내림차순 정렬
                }

                if (historyList.size() != 0) {
                    dataNum = historyList.size();

                    // 페이징 처리
                    if (dataNum > Constant.HISTORY_DATA_NUM) {
                        int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                        int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;

                        List<History> historyList_paging = new ArrayList<>();
                        for (int i = startDataIdx; i < endDataIdx; i++) {
                            historyList_paging.add(historyList.get(i));
                        }
                        historyList = historyList_paging;
                    }
                    historyList_sender.setHistoryList(historyList);

                } else {
                    throw new NullPointerException(); // 검색 결과 없음
                }
            }

            // PagingRes
            int endPage = (int) Math.ceil(dataNum / Constant.HISTORY_DATA_NUM); // 마지막 페이지 번호
            if (pageInfo.getCurrentPage() > endPage) {
                throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
            }
            pageInfo.setEndPage(endPage);
            pageInfo.setHasNext(pageInfo.getCurrentPage() != endPage); // pageNum == endPage -> hasNext = false

            return historyList_sender;

        } catch (NullPointerException nullPointerException) {
            throw new BaseException(EMPTY_RESULT); // 검색 결과 없음
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
