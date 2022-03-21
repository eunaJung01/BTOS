package com.umc.btos.src.history;

import com.umc.btos.config.*;
import com.umc.btos.src.history.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
     * 존재하는 회원 닉네임인지 확인 (User.status 확인 X)
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

            // 발신인
            if (filtering.compareTo("sender") == 0) {
                List<History_Sender> historyListRes_list = new ArrayList<>(); // GetHistoryListRes.list

                dataNum_total = historyDao.getNickNameNum(userIdx, search); // 총 데이터 개수
                if (dataNum_total == 0) {
                    throw new NullPointerException(); // 검색 결과 없음
                }
                pageInfo.setDataNum_total((int) dataNum_total);

                // userIdx 회원이 수신한 모든 항목(일기, 편지, 답장)에 대한 발신인 닉네임 목록 (createdAt 기준 내림차순 정렬 + 닉네임 검색 + 페이징 처리)
                List<String> senderNickNameList = historyDao.getNickNameList(userIdx, search, pageNum);

                for (String senderNickName : senderNickNameList) {
                    int historyListNum = historyDao.getHistoryListNum(userIdx, senderNickName); // 해당 발신인에게서 수신한 모든 항목 개수
                    History firstContent = historyDao.getFirstContent(userIdx, senderNickName); // 수신한 일기, 편지, 답장 중 가장 최근에 받은 값

                    // set History.senderActive
                    historyDao.setSenderActive(firstContent);

                    // type = diary -> set emotionIdx, doneListNum
                    if (firstContent.getType().compareTo("diary") == 0) {
                        int diaryIdx = firstContent.getTypeIdx();
                        firstContent.setEmotionIdx(historyDao.getEmotionIdx(diaryIdx));

                        if (historyDao.hasDone(diaryIdx) == 1) { // 해당 일기에 done list가 있는 경우
                            firstContent.setDoneListNum(historyDao.getDoneListNum(diaryIdx));
                        }
                    }
                    historyListRes_list.add(new History_Sender(historyListNum, firstContent));
                }

                pageInfo.setDataNum_currentPage(historyListRes_list.size()); // 현재 페이지의 데이터 개수

                historyListRes.setList(historyListRes_list);
            }

            // 일기만
            else if (filtering.compareTo("diary") == 0) {
                dataNum_total = historyDao.getDiaryNum(userIdx, search); // 총 데이터 개수
                if (dataNum_total == 0) {
                    throw new NullPointerException(); // 검색 결과 없음
                }
                pageInfo.setDataNum_total((int) dataNum_total);

                // userIdx 회원이 수신한 일기 목록 (createdAt 기준 내림차순 정렬 + 문자열 검색 + 페이징 처리)
                List<History> historyList = historyDao.getDiaryList(userIdx, search, pageNum);
                pageInfo.setDataNum_currentPage(historyList.size()); // 현재 페이지의 데이터 개수

                // set History.senderActive
                historyDao.setSenderActive(historyList);

                // set History.doneListNum
                for (History history : historyList) {
                    int diaryIdx = history.getTypeIdx();
                    if (historyDao.hasDone(diaryIdx) == 1) { // 해당 일기에 done list가 있는 경우
                        history.setDoneListNum(historyDao.getDoneListNum(diaryIdx));
                    }
                }
                historyListRes.setList(historyList);
            }

            // 편지만
            else {
                dataNum_total = historyDao.getLetterNum(userIdx, search); // 총 데이터 개수
                if (dataNum_total == 0) {
                    throw new NullPointerException(); // 검색 결과 없음
                }
                pageInfo.setDataNum_total((int) dataNum_total);

                // userIdx 회원이 수신한 편지 목록 (createdAt 기준 내림차순 정렬 + 문자열 검색 + 페이징 처리)
                List<History> historyList = historyDao.getLetterList(userIdx, search, pageNum);
                pageInfo.setDataNum_currentPage(historyList.size()); // 현재 페이지의 데이터 개수

                // set History.senderActive
                historyDao.setSenderActive(historyList);

                historyListRes.setList(historyList);
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

            // ---------------------------------------------- PagingRes -----------------------------------------------
            int pageNum = pageInfo.getCurrentPage(); // 페이지 번호
            double dataNum_total = 0; // 총 데이터 개수 (후에 Math.ceil 사용하는 연산 때문에 double)
            int dataNum_currentPage = 0; // 현재 페이지의 데이터 개수

            dataNum_total = historyDao.getHistoryListNum_sender(userIdx, senderNickName, search); // 총 데이터 개수
            if (dataNum_total == 0) {
                throw new NullPointerException(); // 검색 결과 없음
            }
            pageInfo.setDataNum_total((int) dataNum_total);

            int endPage = (int) Math.ceil(dataNum_total / Constant.HISTORY_DATA_NUM); // 마지막 페이지 번호
            if (endPage == 0) endPage = 1;
            if (pageInfo.getCurrentPage() > endPage) {
                throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
            }
            pageInfo.setEndPage(endPage);
            pageInfo.setHasNext(pageInfo.getCurrentPage() != endPage); // pageNum == endPage -> hasNext = false
            // --------------------------------------------------------------------------------------------------------

            // 해당 발신인에게서 수신한 모든 항목(일기, 편지, 답장) 목록 (createdAt 기준 내림차순 정렬 + 문자열 검색 + 페이징 처리)
            List<History> historyList = historyDao.getHistoryList_sender(userIdx, senderNickName, search, pageNum); // GetSenderRes.historyList

            dataNum_currentPage = historyList.size(); // 현재 페이지의 데이터 개수
            pageInfo.setDataNum_currentPage(dataNum_currentPage);

            // set senderActive
            historyDao.setSenderActive(historyList);

            // type = diary -> set emotionIdx, doneListNum
            for (History history : historyList) {
                String type = history.getType();

                if (type.compareTo("diary") == 0) {
                    int diaryIdx = history.getTypeIdx();
                    history.setEmotionIdx(diaryIdx);

                    if (historyDao.hasDone(diaryIdx) == 1) { // 해당 일기에 done list가 있는 경우
                        history.setDoneListNum(historyDao.getDoneListNum(diaryIdx));
                    }
                }
            }

            return historyList;

        } catch (NullPointerException exception) {
            throw new BaseException(EMPTY_RESULT); // 검색 결과 없음
        } catch (BaseException exception) {
            throw new BaseException(PAGENUM_ERROR); // 잘못된 페이지 요청입니다.
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
            List<GetHistoryRes> historyList = new ArrayList<>();

            // type = diary
            if (type.compareTo("diary") == 0) {
                GetHistoryRes diary = historyDao.getDiary_main(typeIdx, historyDao.getSenderActive_diary(typeIdx));
                diary.setPositioning(true);
                if (historyDao.hasDone(typeIdx) == 1) { // 해당 일기에 done list가 있는 경우
                    diary.setDoneList(historyDao.getDoneList_main(typeIdx));
                }
                historyList.add(diary);

                if (historyDao.hasReply_diary(userIdx, typeIdx) == 1) { // 답장 유무 확인
                    historyList.addAll(historyDao.getReplyList_diary(userIdx, typeIdx)); // 답장 목록

                    // setSenderActive
                    for (GetHistoryRes history : historyList) {
                        if (history.getType().compareTo("reply") == 0) {
                            int replyIdx = history.getTypeIdx();
                            history.setSenderActive(historyDao.getSenderActive_reply(replyIdx));
                        }
                    }
                }
            }

            // type = letter
            else if (type.compareTo("letter") == 0) {
                GetHistoryRes letter = historyDao.getLetter_main(typeIdx, historyDao.getSenderActive_letter(typeIdx));
                letter.setPositioning(true);
                historyList.add(letter);

                if (historyDao.hasReply_letter(userIdx, typeIdx) == 1) { // 답장 유무 확인
                    historyList.addAll(historyDao.getReplyList_letter(userIdx, typeIdx)); // 답장 목록

                    // setSenderActive
                    for (GetHistoryRes history : historyList) {
                        if (history.getType().compareTo("reply") == 0) {
                            int replyIdx = history.getTypeIdx();
                            history.setSenderActive(historyDao.getSenderActive_reply(replyIdx));
                        }
                    }
                }
            }

            // type = reply
            else {
                int replierIdx = historyDao.getReplierIdx(typeIdx);

                // 시스템 메일인 경우
                if (replierIdx == 1) {
                    GetHistoryRes systemMail = historyDao.getReply_systemMail(typeIdx);
                    systemMail.setSenderActive(false);
                    systemMail.setPositioning(true);
                    historyList.add(systemMail);
                }

                // 일반 답장일 경우
                else {
                    String firstHistoryType = historyDao.getHistoryType(typeIdx); // 답장의 최초 시작점 (diary / letter)

                    // 시작점이 일기인 경우
                    if (firstHistoryType.compareTo("diary") == 0) {
                        int diaryIdx = historyDao.getDiaryIdx_main(typeIdx);

                        GetHistoryRes diary = historyDao.getDiary_main(diaryIdx, historyDao.getSenderActive_diary(diaryIdx));

                        if (historyDao.hasDone(typeIdx) == 1) { // 해당 일기에 done list가 있는 경우
                            diary.setDoneList(historyDao.getDoneList_main(typeIdx));
                        }

                        historyList.add(diary);

                        // 답장 목록
                        List<GetHistoryRes> replyList = historyDao.getReplyList_diary(userIdx, diaryIdx);
                        for (GetHistoryRes reply : replyList) {
                            // setSenderActive
                            int replyIdx = reply.getTypeIdx();
                            reply.setSenderActive(historyDao.getSenderActive_reply(replyIdx));

                            if (replyIdx == typeIdx) { // 사용자가 조회하고자 하는 답장 본문
                                reply.setPositioning(true);
                            }
                        }
                        historyList.addAll(replyList);
                    }

                    // 시작점이 편지인 경우
                    else if (firstHistoryType.compareTo("letter") == 0) {
                        int letterIdx = historyDao.getLetterIdx_main(typeIdx);
                        GetHistoryRes letter = historyDao.getLetter_main(letterIdx, historyDao.getSenderActive_letter(letterIdx));
                        historyList.add(letter);

                        List<GetHistoryRes> replyList = historyDao.getReplyList_letter(userIdx, letterIdx);
                        for (GetHistoryRes reply : replyList) {
                            // setSenderActive
                            int replyIdx = reply.getTypeIdx();
                            reply.setSenderActive(historyDao.getSenderActive_reply(replyIdx));

                            if (replyIdx == typeIdx) { // 사용자가 조회하고자 하는 답장 본문
                                reply.setPositioning(true);
                            }
                        }
                        historyList.addAll(replyList);
                    }
                }
            }

            return historyList;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
