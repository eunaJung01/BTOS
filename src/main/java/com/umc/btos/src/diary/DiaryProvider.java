package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.diary.model.*;
import com.umc.btos.utils.AES128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class DiaryProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DiaryDao diaryDao;

//    @Value("${secret.private-diary-key}")
//    String PRIVATE_DIARY_KEY;

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
     * 일기 조회 - 우편함
     */
    public GetDiaryRes getDiary(int userIdx, int diaryIdx) throws BaseException {
        try {
            GetDiaryRes diary = diaryDao.getDiary(diaryIdx); // 일기의 정보
            diary.setDoneList(diaryDao.getDoneList(diaryIdx)); // done list 정보
            diaryDao.modifyIsChecked(userIdx, diaryIdx); // DiarySendList.isChecked = 1로 변환

            // content 복호화
            if (diaryDao.getIsPublic(diaryIdx) == 0) { // private 일기일 경우 content 복호화
                decryptContents(diary);
            }

            return diary;

        } catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // content 복호화
    public void decryptContents(GetDiaryRes diary) throws BaseException {
        try {
            // Diary.content
            String diaryContent = diary.getContent();
            diary.setContent(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(diaryContent));

            // Done.content
            List<GetDoneRes> doneList = diary.getDoneList();
            for (int j = 0; j < doneList.size(); j++) {
                String doneContent = diary.getDoneList().get(j).getContent();
                diary.getDoneList().get(j).setContent(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(doneContent));
//                diary.getDoneList().get(j).setContent(new AES128(PRIVATE_DIARY_KEY).decrypt(doneContent));
            }

        } catch (Exception ignored) {
            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
        }
    }

    // ============================================ 일기 발송 ============================================

    // Diary - Send Algorithm
    @Scheduled(cron = "55 59 18 * * *") // 매일 18:59:55에 DiarySendList 생성
//    @Scheduled(cron = "00 23 21 * * *") // test
    public void sendDiary() {

        LocalDate yesterday = LocalDate.now().minusDays(1); // 어제 날짜 (yyyy-MM-dd)
        List<Integer> diaryIdxList = diaryDao.getDiaryIdxList(yesterday.toString()); // 당일 발송해야 하는 모든 diaryIdx
//        List<Integer> diaryIdxList = diaryDao.getDiaryIdxList("2022-02-01"); // test

        /*
         * [ 생각해야 하는 경우 3가지 ]
         * 발송해야 할 일기의 개수가 1. 0개일 때 / 2. 1개일 때 / 3. 2개 이상일 때
         * -> 각각의 처리가 필요
         */
        if (diaryIdxList.size() == 0) { // 1. 당일 발송해야 할 일기의 개수가 0개인 경우
            return; // 일기 발송 알고리즘 종료
        }

        Map<Integer, Integer> diaryIdx_sendNumMap = new HashMap<>(); // key = diaryIdx, value = 해당 일기가 발송된 횟수 (현재까지 몇명에게 보내졌는가?)
        for (int diaryIdx : diaryIdxList) {
            diaryIdx_sendNumMap.put(diaryIdx, 0); // 당일 발송해야 하는 일기마다 저장 공간 생성
        }

        List<Integer> userIdxList_total = diaryDao.getUserIdxList_total(); // 수신 동의한 모든 userIdx (User.recOthers = 1)
        int totalUserNum = userIdxList_total.size(); // 일기를 발송 받을 총 회원 수

        Map<Integer, Boolean> userIdx_sendMap = new HashMap<>(); // key = userIdx, value = 일기 발송 유무 (발송되었다면 true, 아직 안 되었다면 false)
        for (int userIdx : userIdxList_total) {
            userIdx_sendMap.put(userIdx, false); // 수신 동의한 회원마다 저장 공간 생성
        }

        // 일기마다 보내져야 하는 횟수 (sendNum ~ sendNum+1)
        int sendNum = totalUserNum / diaryIdxList.size(); // 총 회원 수 / 총 일기 개수
        // sendNum == 일기마다 발송되어야 하는 최소 횟수


        // --------------------------------------- 2. 당일 발송해야 할 일기의 개수가 1개인 경우 ---------------------------------------

        if (diaryIdxList.size() == 1) {
            int diaryIdx = diaryIdxList.get(0);
            int senderUserIdx = diaryDao.getSenderUserIdx(diaryIdx); // 발신인 userIdx
            for (int userIdx : userIdxList_total) {
                if (userIdx != senderUserIdx) {
                    diaryDao.setDiarySendList(diaryIdx, userIdx); // INSERT INTO DiarySendList Table
                }
            }
        }

        // --------------------------------------- 3. 당일 발송해야 할 일기의 개수가 2개 이상인 경우 ---------------------------------------

        // TODO : 비슷한 나이대 발송 처리

        // 2차원 가변 배열
        // [diaryIdxList 인덱스 값][해당 일기 발신인과 비슷한 나이대를 갖는 userIdx]
        else {
            List<List<Integer>> userIdxList_similarAge = new ArrayList<>();

            int i = 0;
            for (int diaryIdx : diaryIdxList) {
                userIdxList_similarAge.add(new ArrayList<>());

                int senderUserIdx = diaryDao.getSenderUserIdx(diaryIdx); // 발신인 userIdx
                int senderBirth = diaryDao.getSenderBirth(diaryIdx); // 발신인 생년

                // 발송 가능한(User.recOthers = 1) & 비슷한 나이대를 갖는(senderBirth -5 ~ +5) 모든 userIdx
                userIdxList_similarAge.get(i).addAll(diaryDao.getUserIdxList_similarAge(senderUserIdx, senderBirth)); // 발신인 userIdx 제외
                i++;
            }

            int userIdxNum_similarAge = diaryDao.getUserIdxNum_similarAge(); // 비슷한 나이대 수신 동의한 회원 수 (recSimilarAge = 1)

            // 일기마다 비슷한 나이대로 보낼 횟수 = 비슷한 나이대 수신 동의한 회원 수 / 당일 발송해야 하는 일기 개수
            // 비슷한 나이대 수신 동의한 회원 수 중 70 ~ 80%는 비슷한 나이로 수신받게 함
            // Constant.DIARY_REC_SIMILAR_AGE_RATIO = 80
            int sendNum_similarAge = userIdxNum_similarAge * Constant.DIARY_REC_SIMILAR_AGE_RATIO / 100 / diaryIdxList.size(); // 일기마다 비슷한 나이대로 보낼 횟수

            int j = 0;
            for (int diaryIdx : diaryIdxList) { // 당일 발송해야 하는 일기 개수만큼
                int userNum_similarAge = userIdxList_similarAge.get(j).size(); // 해당 일기의 발신인과 비슷한 나이대를 갖는 회원의 수 (최초 저장된 값)

                List<Integer> userIdxList_similarAge_updated = new ArrayList<>(); // 해당 일기를 발송할 후보에 드는 회원들 리스트 (후보 회원 리스트)
                for (int k = 0; k < userNum_similarAge; k++) { // 해당 일기의 발신인과 비슷한 나이대를 갖는 회원의 수만큼
                    int userIdx = userIdxList_similarAge.get(j).get(k);
                    if (!userIdx_sendMap.get(userIdx)) { // 회원마다 일기 발송 유무 확인 (Map.value = false -> add)
                        userIdxList_similarAge_updated.add(userIdx);
                    }
                }

                for (int k = 0; k < sendNum_similarAge; k++) { // 일기마다 비슷한 나이대로 보낼 횟수만큼
                    int idx = (int) (Math.random() * userIdxList_similarAge_updated.size()); // 후보 회원 리스트의 인덱스 값을 랜덤으로 반환 (0 ~ 리스트 마지막 인덱스 값)
                    int receiverIdx = userIdxList_similarAge_updated.get(idx); // 수신인 userIdx

                    diaryDao.setDiarySendList(diaryIdx, receiverIdx); // INSERT INTO DiarySendList Table
                    userIdx_sendMap.put(receiverIdx, true); // Map.value = false -> true (해당 회원에게 일기가 발송됨을 체크)

                    for (int l = 0; l < userIdxList_similarAge_updated.size(); l++) { // 후보 회원 리스트 갱신
                        if (userIdxList_similarAge_updated.get(l) == receiverIdx) {
                            userIdxList_similarAge_updated.remove(l); // 방금 일기가 발송된 userIdx -> remove
                        }
                    }
                }
                diaryIdx_sendNumMap.put(diaryIdx, sendNum_similarAge); // 일기 발송 횟수 저장
                j++;
            }


            // TODO : 일반 발송 처리
            // (총 회원 수 / 총 일기 개수)의 몫만큼 발송

            // List<Integer> userIdxList_total = diaryDao.getUserIdxList_total(); // 수신 동의한 모든 userIdx (User.recOthers = 1)
            // -> 일기를 발송 받아야 하는 userIdx 리스트로 갱신
            List<Integer> userIdxList_total_updated = new ArrayList<>();
            for (int userIdx : userIdxList_total) {
                if (!userIdx_sendMap.get(userIdx)) { // 회원마다 일기 발송 유무 확인 (Map.value = false -> add)
                    userIdxList_total_updated.add(userIdx);
                }
            }
            userIdxList_total = userIdxList_total_updated; // 갱신

            for (int userIdx : userIdxList_total) { // 일기를 발송 받아야 하는 회원 수만큼
                List<Integer> diaryIdxList_updated = new ArrayList<>(); // 현재 발송되어야 하는 diaryIdx 목록 (diaryIdxList 갱신)
                for (int diaryIdx : diaryIdxList) {
                    if (diaryIdx_sendNumMap.get(diaryIdx) < sendNum) { // 일기마다 발송된 횟수 확인 (Map.value < 발송되어야 하는 최소 횟수 -> add)
                        diaryIdxList_updated.add(diaryIdx);
                    }
                }

                if (diaryIdxList_updated.size() != 0) { // (총 회원 수 / 총 일기 개수)의 몫만큼
                    // 수신인 userIdx == 발신인 senderUserIdx인 경우 -> 다른 사람의 일기를 수신할 때까지 다시 실행
                    boolean isSend = false;
                    do {
                        isSend = sendDiary_general(diaryIdx_sendNumMap, userIdx_sendMap, userIdx, diaryIdxList_updated);
                    }
                    while (!isSend);
                }
            }


            // TODO : 일반 발송 처리 (나머지)
            // (총 회원 수 / 총 일기 개수)의 나머지 부분 발송

            userIdxList_total_updated = new ArrayList<>(); // 일기를 발송 받아야 하는 모든 userIdx
            for (int userIdx : userIdxList_total) {
                if (!userIdx_sendMap.get(userIdx)) { // 회원마다 일기 발송 유무 확인 (Map.value = false -> add)
                    userIdxList_total_updated.add(userIdx);
                }
            }
            userIdxList_total = userIdxList_total_updated; // 갱신

            if (userIdxList_total.size() != 0) {
                for (int userIdx : userIdxList_total) { // 일기 발송 받아야 하는 회원 수만큼
                    List<Integer> diaryIdxList_updated = new ArrayList<>(); // 발송해야 하는 diaryIdx 목록

                    for (int diaryIdx : diaryIdxList) { // 당일 발송해야 하는 모든 일기 개수만큼
                        if (diaryIdx_sendNumMap.get(diaryIdx) <= sendNum) { // 일기마다 발송된 횟수 확인 (Map.value <= 발송되어야 하는 최소 횟수 -> add)
                            // = : 발송해야 하는 일기가 2개 이상인 경우 / < : 발송해야 하는 일기가 한 개인 경우
                            diaryIdxList_updated.add(diaryIdx);
                        }
                    }

                    // 수신인 userIdx == 발신인 senderUserIdx인 경우 -> 다른 사람의 일기를 수신할 때까지 다시 실행
                    boolean isSend = false;
                    do {
                        isSend = sendDiary_general(diaryIdx_sendNumMap, userIdx_sendMap, userIdx, diaryIdxList_updated);
                    }
                    while (!isSend);
                }
            }
        }

        /*
         * TODO : 매일 19:00:00에 당일 발송되는 일기의 Diary.isSend = 1로 변경
         * diaryDao.modifyIsSend();
         */

    }

    // 일기 발송 (비슷한 나이대 발송 후 일반 발송 처리)
    private boolean sendDiary_general(Map<Integer, Integer> diaryIdx_sendNumMap, Map<Integer, Boolean> userIdx_sendMap, int userIdx, List<Integer> diaryIdxList_updated) {
        int diaryIdx = 0;
        if (diaryIdxList_updated.size() != 0) {
            diaryIdx = diaryIdxList_updated.get((int) (Math.random() * diaryIdxList_updated.size())); // 발송해야 하는 일기 리스트의 인덱스 값을 랜덤으로 반환 (0 ~ 리스트 마지막 인덱스 값)
        }

        int senderUserIdx = diaryDao.getSenderUserIdx(diaryIdx); // 발신인 userIdx

        if (userIdx != senderUserIdx) {
            diaryDao.setDiarySendList(diaryIdx, userIdx); // INSERT INTO DiarySendList Table
            userIdx_sendMap.put(userIdx, true); // 일기 발송 유무 변경 : Map.value = false -> true

            int sendNum_diary = diaryIdx_sendNumMap.get(diaryIdx); // 기존 일기 발송 횟수
            diaryIdx_sendNumMap.put(diaryIdx, ++sendNum_diary); // // 일기 발송 횟수 갱신 (+1)

            return true;

        } else return false; // userIdx == senderUserIdx인 경우 -> return false (sendDiary_general 함수 다시 실행)

    }

    /*
     * 일기 발송 리스트 조회
     * 매일 18:59:59 Firebase에서 호출
     * [GET] /diaries/diarySendList
     */
    public List<GetSendListRes> getDiarySendList() throws BaseException {
        try {
            List<GetSendListRes> result = new ArrayList<>();

            LocalDate yesterday = LocalDate.now().minusDays(1); // 어제 날짜 (yyyy-MM-dd)
            List<Integer> diaryIdxList = diaryDao.getDiaryIdxList(yesterday.toString()); // 당일 발송해야 하는 모든 diaryIdx
//            List<Integer> diaryIdxList = diaryDao.getDiaryIdxList("2022-02-01"); // test

            if (diaryIdxList.size() == 0) {
                throw new BaseException(NO_DIARY_SENT_TODAY); // 오늘 발송되는 일기는 없습니다.
            }

            // 일기에 따른 발송 리스트 조회
            for (int diaryIdx : diaryIdxList) {
                GetSendListRes diary = new GetSendListRes(diaryIdx);
                diary.setReceiverIdxList(diaryDao.getReceiverIdxList(diaryIdx, yesterday.toString()));
                result.add(diary);
            }

            return result;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String dateTest() throws BaseException {
        try {
            Date date = new Date();
            LocalDate today = LocalDate.now(); // 오늘 날짜 (yyyy-MM-dd)

            String result = "Date 객체 : " + date.toString() + " / LocalDate 객체 : " + today.toString();
            return result;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
