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
     * 존재하는 회원인지 확인
     */
    public int checkUserIdx(int userIdx) throws BaseException {
        try {
            return historyDao.checkUserIdx(userIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 존재하는 회원 닉네임인지 확인
     */
    public int checkNickName(String senderNickName) throws BaseException {
        try {
            return historyDao.checkNickName(senderNickName); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 해당 type에 존재하는 typeIdx인지 확인
     */
    public int checkTypeIdx(String type, int typeIdx) throws BaseException {
        try {
            if (type.compareTo("diary") == 0) {
                return historyDao.checkDiaryIdx(typeIdx);

            } else if (type.compareTo("letter") == 0) {
                return historyDao.checkLetterIdx(typeIdx);

            } else {
                return historyDao.checkReplyIdx(typeIdx);
            }

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================

    /*
     * History 목록 조회
     * [GET] /histories/list/:userIdx/:pageNum?filtering=&search=
     * filtering = 1. sender : 발신인 (Diary, Letter, Reply) / 2. diary : 일기만 (Diary) / 3. letter : 편지만 (Letter, Reply)
     * search (검색할 문자열 "String") = 1. filtering = sender : 닉네임 검색 / 2. filtering = diary or letter : 내용 검색
     * 검색 시 띄어쓰기, 영문 대소문자 구분없이 조회됨
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
            double dataNum_total = 0; // 총 데이터 개수 (후에 Math.ceil 사용하는 연산 때문에 double)
            int dataNum_currentPage = 0; // 현재 페이지의 데이터 개수

            GetHistoryListRes historyListRes = new GetHistoryListRes();
            /*
             * filtering = "sender"인 경우 GetHistoryListRes.list = List<History_Sender>
             * filtering = "diary" 또는 "letter"인 경우 GetHistoryListRes.list = List<History>
             * History 객체 : 수신한 일기/편지/답장에 대한 상세 정보를 저장
             */

            if (search == null) {
                // 발신인
                if (filtering.compareTo("sender") == 0) {
                    List<History_Sender> historyListRes_list = new ArrayList<>(); // GetHistoryListRes.list

                    // userIdx 회원이 받은 일기, 편지, 답장의 발신인 닉네임 목록 (createdAt 기준 내림차순 정렬)
                    List<String> senderNickNameList = historyDao.getNickNameList_sortedByCreatedAt(userIdx);

                    dataNum_total = senderNickNameList.size(); // 총 데이터 개수
                    if (dataNum_total == 0) {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                    pageInfo.setDataNum_total((int) dataNum_total);

                    // 페이징 처리
                    if (dataNum_total > Constant.HISTORY_DATA_NUM) {
                        int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                        int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;
                        if (endDataIdx > dataNum_total) endDataIdx = (int) dataNum_total;

                        List<String> senderNickNameList_paging = new ArrayList<>();
                        for (int i = startDataIdx; i < endDataIdx; i++) {
                            senderNickNameList_paging.add(senderNickNameList.get(i));
                        }
                        senderNickNameList = senderNickNameList_paging; // senderNickNameList 갱신
                    }

                    setHistoryListRes_list(userIdx, historyListRes_list, senderNickNameList); // HistoryList_Sender 객체 생성 -> historyListRes_list에 저장
                    dataNum_currentPage = historyListRes_list.size(); // 현재 페이지의 데이터 개수
                    pageInfo.setDataNum_currentPage(dataNum_currentPage);

                    historyListRes.setList(historyListRes_list);
                }

                // 일기만
                else if (filtering.compareTo("diary") == 0) {
                    List<History> historyList = new ArrayList<>(); // GetHistoryListRes.list

                    dataNum_total = historyDao.getDiaryIdxList_dataNum(userIdx); // 총 데이터 개수
                    if (dataNum_total == 0) {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                    pageInfo.setDataNum_total((int) dataNum_total);

                    List<Integer> diaryIdxList = historyDao.getDiaryIdxList(userIdx, pageNum); // 수신한 일기들 (createdAt 기준 내림차순 정렬, 페이징 처리)
                    dataNum_currentPage = diaryIdxList.size(); // 현재 페이지의 데이터 개수
                    pageInfo.setDataNum_currentPage(dataNum_currentPage);

                    for (int diaryIdx : diaryIdxList) {
                        if (historyDao.hasDone(diaryIdx) == 1) { // 해당 일기에 done list가 있는 경우
                            historyList.add(historyDao.getDiary_done(userIdx, diaryIdx, historyDao.getSenderActive_diary(diaryIdx)));
                        } else { // 해당 일기에 done list가 없는 경우
                            historyList.add(historyDao.getDiary_nonDone(userIdx, diaryIdx, historyDao.getSenderActive_diary(diaryIdx)));
                        }
                    }

                    historyListRes.setList(historyList);
                }

                // 편지만
                else {
                    List<History> historyList = new ArrayList<>(); // GetHistoryListRes.list

                    if (historyDao.hasHistory_letter(userIdx) != 0) { // 수신한 편지가 있는지 확인
                        historyList.addAll(historyDao.getLetterList(userIdx)); // 편지 (createAt 기준 내림차순 정렬)
                    }
                    if (historyDao.hasHistory_reply(userIdx) != 0) { // 수신한 답장이 있는지 확인
                        historyList.addAll(historyDao.getReplyList(userIdx)); // 답장 (createAt 기준 내림차순 정렬)
                    }
                    Collections.sort(historyList);

                    dataNum_total = historyList.size(); // 총 데이터 개수
                    if (dataNum_total == 0) {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                    pageInfo.setDataNum_total((int) dataNum_total);

                    // 페이징 처리
                    if (dataNum_total > Constant.HISTORY_DATA_NUM) {
                        int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                        int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;
                        if (endDataIdx > dataNum_total) endDataIdx = (int) dataNum_total;

                        List<History> historyList_paging = new ArrayList<>();
                        for (int i = startDataIdx; i < endDataIdx; i++) {
                            historyList_paging.add(historyList.get(i));
                        }
                        historyList = historyList_paging; // historyList 갱신
                    }
                    dataNum_currentPage = historyList.size(); // 현재 페이지의 데이터 개수
                    pageInfo.setDataNum_currentPage(dataNum_currentPage); // 현재 페이지의 데이터 개수

                    // setSenderActive
                    for (History history : historyList) {
                        int typeIdx = history.getTypeIdx();

                        switch (history.getType()) {
                            case "letter" :
                                history.setSenderActive(historyDao.getSenderActive_letter(typeIdx));
                                break;
                            case "reply" :
                                history.setSenderActive(historyDao.getSenderActive_reply(typeIdx));
                                break;
                        }
                    }
                    historyListRes.setList(historyList);
                }

            } else {
                /*
                 * 문자열 검색 (search)
                 * search && (Diary.content || Letter.content) : 띄어쓰기 모두 제거 후 찾기
                 *
                 * 1. filtering = sender(발신인) -> 닉네임 검색
                 * 2. filtering = diary(일기만) & letter(편지만) -> Diary.content 또는 Letter.content 검색
                 */

                search = search.replaceAll("\"", ""); // 따옴표 제거
                search = search.replaceAll(" ", ""); // 공백 제거
                search = search.toLowerCase(); // 영문 대소문자 구분 X

                // 발신인
                if (filtering.compareTo("sender") == 0) {
                    List<History_Sender> historyListRes_list = new ArrayList<>(); // GetHistoryListRes.list

                    // userIdx 회원이 받은 일기, 편지, 답장의 발신자 닉네임 목록 (createdAt 기준 내림차순 정렬)
                    List<String> senderNickNameList = historyDao.getNickNameList_sortedByCreatedAt(userIdx);

                    List<String> senderNickName_searched = new ArrayList<>();
                    for (String senderNickName : senderNickNameList) {
                        if (searchString(senderNickName, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                            senderNickName_searched.add(senderNickName);
                        }
                    }
                    senderNickNameList = senderNickName_searched; // senderNickNameList 갱신

                    dataNum_total = senderNickNameList.size(); // 총 데이터 개수
                    if (dataNum_total == 0) {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                    pageInfo.setDataNum_total((int) dataNum_total);

                    // 페이징 처리
                    if (dataNum_total > Constant.HISTORY_DATA_NUM) {
                        int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                        int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;
                        if (endDataIdx > dataNum_total) endDataIdx = (int) dataNum_total;

                        List<String> senderNickName_paging = new ArrayList<>();
                        for (int i = startDataIdx; i < endDataIdx; i++) {
                            senderNickName_paging.add(senderNickNameList.get(i));
                        }
                        senderNickNameList = senderNickName_paging; // senderNickNameList 갱신
                    }
                    dataNum_currentPage = senderNickNameList.size(); // 현재 페이지의 데이터 개수
                    pageInfo.setDataNum_currentPage(dataNum_currentPage);

                    setHistoryListRes_list(userIdx, historyListRes_list, senderNickNameList); // HistoryList_Sender 객체 생성 -> historyListRes_list에 저장
                    historyListRes.setList(historyListRes_list);
                }

                // 일기만
                else if (filtering.compareTo("diary") == 0) {
                    List<Integer> diaryIdxList = new ArrayList<>();

                    if (historyDao.hasHistory_diary(userIdx) != 0) { // 수신한 일기가 있는지 확인
                        diaryIdxList.addAll(historyDao.getDiaryIdxList(userIdx)); // 수신한 모든 일기 diaryIdx

                        List<Integer> diaryIdxList_searched = new ArrayList<>();
                        for (int diaryIdx : diaryIdxList) {
                            String diaryContent = historyDao.getDiaryContent(diaryIdx);

                            if (searchString(diaryContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                                diaryIdxList_searched.add(diaryIdx);
                            }
                        }
                        diaryIdxList = diaryIdxList_searched; // diaryIdxList 갱신
                    }
                    dataNum_total = diaryIdxList.size(); // 총 데이터 개수
                    if (dataNum_total == 0) {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                    pageInfo.setDataNum_total((int) dataNum_total);

                    if (dataNum_total != 0) {
                        // 페이징 처리
                        if (dataNum_total > Constant.HISTORY_DATA_NUM) {
                            int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                            int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;
                            if (endDataIdx > dataNum_total) endDataIdx = (int) dataNum_total;

                            List<Integer> diaryIdxList_paging = new ArrayList<>();
                            for (int i = startDataIdx; i < endDataIdx; i++) {
                                diaryIdxList_paging.add(diaryIdxList.get(i));
                            }
                            diaryIdxList = diaryIdxList_paging; // diaryIdxList 갱신
                        }
                        dataNum_currentPage = diaryIdxList.size(); // 현재 페이지의 데이터 개수
                        pageInfo.setDataNum_currentPage(dataNum_currentPage);

                        List<History> historyList = new ArrayList<>(); // HistoryList_Sender.historyList
                        for (int diaryIdx : diaryIdxList) {
                            if (historyDao.hasDone(diaryIdx) == 1) { // 해당 일기에 done list가 있는 경우
                                historyList.add(historyDao.getDiary_done(userIdx, diaryIdx, historyDao.getSenderActive_diary(diaryIdx)));
                            } else { // 해당 일기에 done list가 없는 경우
                                historyList.add(historyDao.getDiary_nonDone(userIdx, diaryIdx, historyDao.getSenderActive_diary(diaryIdx)));
                            }
                        }
                        Collections.sort(historyList); // createAt 기준 내림차순 정렬

                        historyListRes.setList(historyList);
                    }
                }

                // 편지만
                else {
                    List<Integer> letterIdxList = new ArrayList<>();
                    List<Integer> replyIdxList = new ArrayList<>();

                    // letter
                    if (historyDao.hasHistory_letter(userIdx) != 0) { // 수신한 편지가 있는지 확인
                        letterIdxList.addAll(historyDao.getLetterIdxList(userIdx)); // 수신한 모든 편지 letterIdx

                        List<Integer> letterIdxList_searched = new ArrayList<>();
                        for (int letterIdx : letterIdxList) {
                            String letterContent = historyDao.getLetterContent(letterIdx);

                            if (searchString(letterContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                                letterIdxList_searched.add(letterIdx);
                            }
                        }
                        letterIdxList = letterIdxList_searched; // letterIdxList 갱신
                    }

                    // reply
                    if (historyDao.hasHistory_reply(userIdx) != 0) { // 수신한 답장이 있는지 확인
                        replyIdxList.addAll(historyDao.getReplyIdxList(userIdx)); // 수신한 모든 답장 replyIdx

                        List<Integer> replyIdxList_searched = new ArrayList<>();
                        for (int replyIdx : replyIdxList) {
                            String replyContent = historyDao.getReplyContent(replyIdx);

                            if (searchString(replyContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                                replyIdxList_searched.add(replyIdx);
                            }
                        }
                        replyIdxList = replyIdxList_searched; // replyIdxList 갱신
                    }

                    if (letterIdxList.size() != 0 || replyIdxList.size() != 0) {
                        List<History> historyList = new ArrayList<>(); // HistoryList_Sender.historyList

                        for (int letterIdx : letterIdxList) {
                            historyList.add(historyDao.getLetter(userIdx, letterIdx, historyDao.getSenderActive_letter(letterIdx)));
                        }
                        for (int replyIdx : replyIdxList) {
                            historyList.add(historyDao.getReply(userIdx, replyIdx, historyDao.getSenderActive_reply(replyIdx)));
                        }
                        dataNum_total = historyList.size(); // 총 데이터 개수
                        pageInfo.setDataNum_total((int) dataNum_total);
                        Collections.sort(historyList); // createAt 기준 내림차순 정렬

                        // 페이징 처리
                        if (dataNum_total > Constant.HISTORY_DATA_NUM) {
                            int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                            int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;
                            if (endDataIdx > dataNum_total) endDataIdx = (int) dataNum_total;

                            List<History> historyList_paging = new ArrayList<>();
                            for (int i = startDataIdx; i < endDataIdx; i++) {
                                historyList_paging.add(historyList.get(i));
                            }
                            historyList = historyList_paging; // historyList 갱신
                        }
                        dataNum_currentPage = historyList.size(); // 현재 페이지의 데이터 개수
                        pageInfo.setDataNum_currentPage(dataNum_currentPage);

                        historyListRes.setList(historyList);
                    } else {
                        throw new NullPointerException(); // 검색 결과 없음
                    }
                }
            }

            // PagingRes
            int endPage = (int) Math.ceil(dataNum_total / Constant.HISTORY_DATA_NUM); // 마지막 페이지 번호
            if (endPage == 0) endPage = 1;
            if (pageInfo.getCurrentPage() > endPage) {
                throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
            }
            pageInfo.setEndPage(endPage);
            pageInfo.setHasNext(pageInfo.getCurrentPage() != endPage); // pageNum == endPage -> hasNext = false

            return historyListRes;

        } catch (BaseException exception) {
            throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
        } catch (NullPointerException exception) {
            throw new BaseException(EMPTY_RESULT); // 검색 결과 없음
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // filtering = sender
    private void setHistoryListRes_list(int userIdx, List<History_Sender> historyListRes_list, List<String> senderNickNameList) {
        for (String senderNickName : senderNickNameList) {
            List<History> historyList = new ArrayList<>(); // HistoryList_Sender.firstContent

            // diary
            if (historyDao.hasHistory_diary(userIdx, senderNickName) != 0) { // 해당 회원에게서 수신한 일기가 있는지 확인
                if (historyDao.hasDone(userIdx, senderNickName) == 1) { // 해당 일기에 done list가 있는 경우
                    historyList.add(historyDao.getDiary_done(userIdx, senderNickName, historyDao.getSenderActive_diary(userIdx, senderNickName))); // 가장 최근에 받은 일기
                } else { // 해당 일기에 done list가 없는 경우
                    historyList.add(historyDao.getDiary_nonDone(userIdx, senderNickName, historyDao.getSenderActive_diary(userIdx, senderNickName))); // 가장 최근에 받은 일기
                }
            }
            // letter
            if (historyDao.hasHistory_letter(userIdx, senderNickName) != 0) { // 해당 회원에게서 수신한 편지가 있는지 확인
                historyList.add(historyDao.getLetter(userIdx, senderNickName, historyDao.getSenderActive_letter(userIdx, senderNickName))); // 가장 최근에 받은 편지
            }
            // reply
            if (historyDao.hasHistory_reply(userIdx, senderNickName) != 0) { // 해당 회원에게서 수신한 편지가 있는지 확인
                historyList.add(historyDao.getReply(userIdx, senderNickName, historyDao.getSenderActive_reply(userIdx, senderNickName))); // 가장 최근에 받은 답장
            }
            Collections.sort(historyList); // createAt 기준 내림차순 정렬

            int historyListNum = historyDao.getDiaryListSize(userIdx, senderNickName) + historyDao.getLetterListSize(userIdx, senderNickName) + historyDao.getReplyListSize(userIdx, senderNickName);

            History_Sender historyList_sender = new History_Sender(historyListNum, historyList.get(0)); // 수신한 일기, 편지, 답장 중 가장 최근에 받은 값
            historyListRes_list.add(historyList_sender);
        }
    }

    // 문자열 검색
    public boolean searchString(String content, String search) {
        content = content.replaceAll(" ", "").toLowerCase(); // 공백 제거, 영문 대소문자 구별 X
        return content.contains(search); // 문자열 검색 (존재 : true, 미존재 : false)
    }

    /*
     * History 발신인 조회
     * [GET] /histories/sender/:userIdx/:senderNickName/:pageNum?search=
     * search = 검색할 문자열 ("String")
     * 검색 시 띄어쓰기, 영문 대소문자 구분없이 조회됨
     * 최신순 정렬 (createdAt 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    public List<History> getHistoryList_sender(String[] params, PagingRes pageInfo) throws BaseException {
        try {
            // String[] params = new String[]{userIdx, senderNickName, search};
            int userIdx = Integer.parseInt(params[0]);
            String senderNickName = params[1];
            String search = params[2];

            // PagingRes
            int pageNum = pageInfo.getCurrentPage(); // 페이지 번호
            double dataNum_total = 0; // 총 데이터 개수 (후에 Math.ceil 사용하는 연산 때문에 double)
            int dataNum_currentPage = 0; // 현재 페이지의 데이터 개수

            List<History> historyList = new ArrayList<>(); // GetSenderRes.historyList

            if (search == null) {
                if (historyDao.hasHistory_diary(userIdx, senderNickName) != 0) { // null 확인
                    List<Integer> diaryIdxList = historyDao.getDiaryIdxList(userIdx, senderNickName); // 수신한 모든 일기 diaryIdx
                    for (int diaryIdx : diaryIdxList) {
                        if (historyDao.hasDone(diaryIdx) == 1) { // 해당 일기에 done list가 있는 경우
                            historyList.add(historyDao.getDiary_done(userIdx, diaryIdx, historyDao.getSenderActive_diary(diaryIdx)));
                        } else { // 해당 일기에 done list가 없는 경우
                            historyList.add(historyDao.getDiary_nonDone(userIdx, diaryIdx, historyDao.getSenderActive_diary(diaryIdx)));
                        }
                    }
                }
                if (historyDao.hasHistory_letter(userIdx, senderNickName) != 0) { // null 확인
                    historyList.addAll(historyDao.getLetterList(userIdx, senderNickName)); // 편지
                }
                if (historyDao.hasHistory_reply(userIdx, senderNickName) != 0) { // null 확인
                    historyList.addAll(historyDao.getReplyList(userIdx, senderNickName)); // 답장
                }
                dataNum_total = historyList.size();
                if (dataNum_total == 0) {
                    throw new NullPointerException(); // 검색 결과 없음
                }
                pageInfo.setDataNum_total((int) dataNum_total); // 총 데이터 개수
                Collections.sort(historyList); // createAt 기준 내림차순 정렬

                // 페이징 처리
                if (dataNum_total > Constant.HISTORY_DATA_NUM) {
                    int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                    int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;
                    if (endDataIdx > dataNum_total) endDataIdx = (int) dataNum_total;

                    List<History> historyList_paging = new ArrayList<>();
                    for (int i = startDataIdx; i < endDataIdx; i++) {
                        historyList_paging.add(historyList.get(i));
                    }
                    historyList = historyList_paging;
                }
                dataNum_currentPage = historyList.size();
                pageInfo.setDataNum_currentPage(dataNum_currentPage); // 현재 페이지의 데이터 개수

            } else {
                /*
                 * 문자열 검색 (search)
                 * search && (Diary.content || Letter.content) : 띄어쓰기 모두 제거 후 찾기
                 */

                search = search.replaceAll("\"", ""); // 따옴표 제거
                search = search.replaceAll(" ", ""); // 공백 제거
                search = search.toLowerCase(); // 영문 대소문자 구분 X

                List<Integer> diaryIdxList = new ArrayList<>();
                List<Integer> letterIdxList = new ArrayList<>();
                List<Integer> replyIdxList = new ArrayList<>();

                // diaryIdxList
                if (historyDao.hasHistory_diary(userIdx, senderNickName) != 0) { // 수신한 일기가 있는지 확인
                    diaryIdxList.addAll(historyDao.getDiaryIdxList(userIdx, senderNickName)); // 수신한 모든 일기 diaryIdx

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
                if (historyDao.hasHistory_letter(userIdx, senderNickName) != 0) { // 수신한 편지가 있는지 확인
                    letterIdxList.addAll(historyDao.getLetterIdxList(userIdx, senderNickName)); // 수신한 모든 편지 letterIdx

                    List<Integer> letterIdxList_searched = new ArrayList<>(); // 임시 list
                    for (int letterIdx : letterIdxList) {
                        String letterContent = historyDao.getLetterContent(letterIdx);

                        if (searchString(letterContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                            letterIdxList_searched.add(letterIdx);
                        }
                    }
                    letterIdxList = letterIdxList_searched; // letterIdxList 갱신
                }

                // replyIdxList
                if (historyDao.hasHistory_reply(userIdx, senderNickName) != 0) { // null 확인
                    replyIdxList.addAll(historyDao.getReplyIdxList(userIdx, senderNickName)); // 수신한 모든 답장 replyIdx

                    List<Integer> replyIdxList_searched = new ArrayList<>(); // 임시 list
                    for (int replyIdx : replyIdxList) {
                        String replyContent = historyDao.getReplyContent(replyIdx);

                        if (searchString(replyContent, search)) { // 문자열 검색 -> 찾는 값이 존재하면 저장
                            replyIdxList_searched.add(replyIdx);
                        }
                    }
                    replyIdxList = replyIdxList_searched; // replyIdxList 갱신
                }

                if (diaryIdxList.size() != 0 || letterIdxList.size() != 0 || replyIdxList.size() != 0) {
                    for (int diaryIdx : diaryIdxList) {
                        if (historyDao.hasDone(diaryIdx) == 1) { // 해당 일기에 done list가 있는 경우
                            historyList.add(historyDao.getDiary_done(userIdx, diaryIdx, senderNickName, historyDao.getSenderActive_diary(diaryIdx)));
                        } else { // 해당 일기에 done list가 없는 경우
                            historyList.add(historyDao.getDiary_nonDone(userIdx, diaryIdx, senderNickName, historyDao.getSenderActive_diary(diaryIdx)));
                        }
                    }

                    for (int letterIdx : letterIdxList) {
                        historyList.add(historyDao.getLetter(userIdx, letterIdx, historyDao.getSenderActive_letter(letterIdx)));
                    }
                    for (int replyIdx : replyIdxList) {
                        historyList.add(historyDao.getReply(userIdx, replyIdx, historyDao.getSenderActive_reply(replyIdx)));
                    }
                    Collections.sort(historyList); // createAt 기준 내림차순 정렬
                }
                dataNum_total = historyList.size(); // 총 데이터 개수
                if (dataNum_total == 0) {
                    throw new NullPointerException(); // 검색 결과 없음
                }
                pageInfo.setDataNum_total((int) dataNum_total);

                // 페이징 처리
                if (dataNum_total > Constant.HISTORY_DATA_NUM) {
                    int startDataIdx = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
                    int endDataIdx = pageNum * Constant.HISTORY_DATA_NUM;
                    if (endDataIdx > dataNum_total) endDataIdx = (int) dataNum_total;

                    List<History> historyList_paging = new ArrayList<>();
                    for (int i = startDataIdx; i < endDataIdx; i++) {
                        historyList_paging.add(historyList.get(i));
                    }
                    historyList = historyList_paging;
                }
                dataNum_currentPage = historyList.size();
                pageInfo.setDataNum_currentPage(dataNum_currentPage); // 현재 페이지의 데이터 개수
            }

            // PagingRes
            int endPage = (int) Math.ceil(dataNum_total / Constant.HISTORY_DATA_NUM); // 마지막 페이지 번호
            if (endPage == 0) endPage = 1;
            if (pageInfo.getCurrentPage() > endPage) {
                throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
            }
            pageInfo.setEndPage(endPage);
            pageInfo.setHasNext(pageInfo.getCurrentPage() != endPage); // pageNum == endPage -> hasNext = false

            return historyList;

        } catch (NullPointerException nullPointerException) {
            throw new BaseException(EMPTY_RESULT); // 검색 결과 없음
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * History 본문 보기 (일기 or 편지 & 답장 리스트)
     * [GET] /histories/:userIdx/:type/:typeIdx
     * type = 조회하고자 하는 본문의 type (일기일 경우 diary, 편지일 경우 letter, 답장일 경우 reply)
     * typeIdx = 조회하고자 하는 본문의 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
     * createdAt 기준 오름차순 정렬
     */
    public List<GetHistoryRes> getHistory_main(int userIdx, String type, int typeIdx) throws BaseException {
        try {
            List<GetHistoryRes> history = new ArrayList<>();

            // type = diary
            if (type.compareTo("diary") == 0) {
                GetHistoryRes diary = historyDao.getDiary_main(typeIdx, historyDao.getSenderActive_diary(typeIdx));
                diary.setPositioning(true);
                if (historyDao.hasDone(typeIdx) == 1) { // 해당 일기에 done list가 있는 경우
                    diary.setDoneList(historyDao.getDoneList_main(typeIdx));
                }

                history.add(diary);
                history.addAll(historyDao.getReplyList_diary(userIdx, typeIdx, historyDao.getSenderActive_reply(typeIdx))); // 답장 목록
            }

            // type = letter
            else if (type.compareTo("letter") == 0) {
                GetHistoryRes letter = historyDao.getLetter_main(typeIdx, historyDao.getSenderActive_reply(typeIdx));
                letter.setPositioning(true);

                history.add(letter);
                history.addAll(historyDao.getReplyList_letter(userIdx, typeIdx, historyDao.getSenderActive_reply(typeIdx))); // 답장 목록
            }

            // type = reply
            else {
                String firstHistoryType = historyDao.getHistoryType(typeIdx); // 답장의 최초 시작점 (diary / letter)

                // 시작점이 일기인 경우
                if (firstHistoryType.compareTo("diary") == 0) {
                    int diaryIdx = historyDao.getDiaryIdx_main(typeIdx);
                    GetHistoryRes diary = historyDao.getDiary_main(diaryIdx, historyDao.getSenderActive_diary(typeIdx));
                    if (historyDao.hasDone(typeIdx) == 1) { // 해당 일기에 done list가 있는 경우
                        diary.setDoneList(historyDao.getDoneList_main(typeIdx));
                    }
                    history.add(diary);

                    // 답장 목록
                    List<GetHistoryRes> replyList = historyDao.getReplyList_diary(userIdx, diaryIdx, historyDao.getSenderActive_diary(diaryIdx));
                    for (GetHistoryRes reply : replyList) {
                        if (reply.getTypeIdx() == typeIdx) { // 사용자가 조회하고자 하는 답장 본문
                            reply.setPositioning(true);
                        }
                    }
                    history.addAll(replyList);
                }

                // 시작점이 편지인 경우
                else if (firstHistoryType.compareTo("letter") == 0) {
                    int letterIdx = historyDao.getLetterIdx_main(typeIdx);
                    GetHistoryRes letter = historyDao.getLetter_main(letterIdx, historyDao.getSenderActive_letter(letterIdx));
                    history.add(letter);

                    List<GetHistoryRes> replyList = historyDao.getReplyList_letter(userIdx, letterIdx, historyDao.getSenderActive_letter(typeIdx));
                    for (GetHistoryRes reply : replyList) {
                        if (reply.getTypeIdx() == typeIdx) { // 사용자가 조회하고자 하는 답장 본문
                            reply.setPositioning(true);
                        }
                    }
                    history.addAll(replyList);
                }
            }

            return history;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
