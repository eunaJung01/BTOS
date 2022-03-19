package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.src.alarm.AlarmService;
import com.umc.btos.src.diary.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
@EnableScheduling
public class DiaryProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DiaryDao diaryDao;
    private final AlarmService alarmService;

    @Value("${secret.private-diary-key}")
    String PRIVATE_DIARY_KEY;

    @Autowired
    public DiaryProvider(DiaryDao diaryDao, AlarmService alarmService) {
        this.diaryDao = diaryDao;
        this.alarmService = alarmService;
    }

    // ================================================== validation ==================================================

    /*
     * 회원 확인 (존재 유무, status)
     */
    public int checkUserIdx(int userIdx) throws BaseException {
        try {
            return diaryDao.checkUserIdx(userIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 일기 확인 (존재 유무, status)
     */
    public int checkDiaryIdx(int diaryIdx) throws BaseException {
        try {
            return diaryDao.checkDiaryIdx(diaryIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 해당 회원이 작성한 일기인지 확인
     */
    public int checkUserAboutDiary(int userIdx, int diaryIdx) throws BaseException {
        try {
            return diaryDao.checkUserAboutDiary(userIdx, diaryIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================================================

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

    // ================================================================================================================

    /*
     * 일기 조회 - 우편함
     */
//    public GetDiaryRes getDiary(int userIdx, int diaryIdx) throws BaseException {
//        try {
//            GetDiaryRes diary = diaryDao.getDiary(diaryIdx); // 일기의 정보
//            diary.setDoneList(diaryDao.getDoneList(diaryIdx)); // done list 정보
//            diaryDao.modifyIsChecked(userIdx, diaryIdx); // DiarySendList.isChecked = 1로 변환
//
//            // content 복호화
//            if (diaryDao.getIsPublic(diaryIdx) == 0) { // private 일기일 경우 content 복호화
//                decryptContents(diary);
//            }
//
//            return diary;
//
//        } catch (Exception exception) {
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }
//
//    // content 복호화
//    public void decryptContents(GetDiaryRes diary) throws BaseException {
//        try {
//            // Diary.content
//            String diaryContent = diary.getContent();
//            diary.setContent(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(diaryContent));
//
//            // Done.content
//            List<Done> doneList = diary.getDoneList();
//            for (int j = 0; j < doneList.size(); j++) {
//                String doneContent = diary.getDoneList().get(j).getContent();
//                diary.getDoneList().get(j).setContent(new AES128(Secret.PRIVATE_DIARY_KEY).decrypt(doneContent));
////                diary.getDoneList().get(j).setContent(new AES128(PRIVATE_DIARY_KEY).decrypt(doneContent));
//            }
//
//        } catch (Exception ignored) {
//            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
//        }
//    }

    // ================================================== 일기 발송 ===================================================

    // TODO: Diary - Send Algorithm
    @Scheduled(cron = "55 59 18 * * *") // 매일 18:59:55에 DiarySendList 생성
//    @Scheduled(cron = "30 51 17 * * *") // test
    public void sendDiary() throws BaseException {
        String yesterday = LocalDate.now().minusDays(1).toString().replaceAll("-", "."); // 어제 날짜 (yyyy.MM.dd)
        List<Integer> diaryIdxList = diaryDao.getDiaryIdxList(yesterday); // 당일 발송해야 하는 모든 diaryIdx
//        List<Integer> diaryIdxList = diaryDao.getDiaryIdxList("2022.03.17"); // test
//        System.out.println("diaryIdxList = " + diaryIdxList);
//        System.out.println();

        /*
         * 당일 발송 가능한 회원(User.recOthers = 1) 당 일기 하나씩 발송
         *
         * 조건
         *      1. 본인 제외
         *      2. 가장 최근에 발신인의 일기를 받은 회원 제외 (연속해서 같은 회원의 편지를 발송받지 않도록 하기 위함)
         *      3. 비슷한 나이대 : 비슷한 나이대로 수신에 동의한 회원들(User.recSimilarAge = 1) 중 발신인의 생년 -5 ~ +5
         *
         *
         * 발송해야 할 일기의 개수에 따른 경우의 수 3가지
         *      1. 0개인 경우
         *          바로 일기 발송 알고리즘 종료
         *
         *      2. 1개인 경우
         *          당일 발송 가능한 모든 회원에게 해당 일기를 발송
         *
         *      3. 2개 이상인 경우
         *          한 일기 당 발송되어야 하는 최소 횟수 : sendNum
         *
         *          (1) 비슷한 나이대 발송
         *              비슷한 나이대 회원들 집단의 70 ~ 80%에게 비슷한 나이대로 발송(n명) - 다양성을 위함
         *              일기 당 회원 무작위
         *
         *          (2) 일반 발송
         *              (sendNum - n)명에게 무작위로 발송
         *              회원 당 일기 무작위
         *
         *          (3) 일반 발송 처리 후 나머지 회원들에 대한 처리
         *              회원 당 일기 무작위
         */

        // ------------------------------- 1. 당일 발송해야 할 일기의 개수가 0개인 경우 -------------------------------

        if (diaryIdxList.size() == 0) {
            return; // 일기 발송 알고리즘 종료
        }

        // ------------------------------------------------------------------------------------------------------------

        Map<Integer, Integer> diaryIdx_sendNumMap = new HashMap<>(); // key = diaryIdx, value = 해당 일기가 발송된 횟수 (현재까지 몇명에게 보내졌는가?)
        for (int diaryIdx : diaryIdxList) {
            diaryIdx_sendNumMap.put(diaryIdx, 0); // 당일 발송해야 하는 일기마다 저장 공간 생성
        }

        List<User> userList_total = diaryDao.getUserList_total(); // 일기 발송 가능한 회원들의 목록 (userIdx)
//        for (User user : userList_total) {
//            System.out.println("userIdx = " + user.getUserIdx());
//        }
//        System.out.println();

        // set userIdx_recentReceived
        for (User user : userList_total) {
            int userIdx = user.getUserIdx();
            user.setUserIdx_recentReceived(diaryDao.getUserIdx_recentReceived(userIdx)); // 가장 최근에 수신한 일기의 발신인 userIdx 저장 (수신한 일기가 없다면 0)
        }

        int totalUserNum = userList_total.size(); // 일기를 발송받을 총 회원 수

        Map<Integer, Boolean> userIdx_sendMap = new HashMap<>(); // key = userIdx, value = 일기 발송 유무 (발송되었다면 true, 아직 안 되었다면 false)
        for (User user : userList_total) {
            int userIdx = user.getUserIdx();
            userIdx_sendMap.put(userIdx, false); // 일기 발송이 가능한 회원마다 저장 공간 생성
        }

        // 일기마다 보내져야 하는 횟수 (sendNum ~ sendNum+1)
        int sendNum = totalUserNum / diaryIdxList.size(); // 총 회원 수 / 총 일기 개수
        // sendNum == 일기마다 발송되어야 하는 최소 횟수
//        System.out.println("sendNum = " + sendNum);
//        System.out.println();

        // ------------------------------- 2. 당일 발송해야 할 일기의 개수가 1개인 경우 -------------------------------

        if (diaryIdxList.size() == 1) {
            int diaryIdx = diaryIdxList.get(0);
            int senderUserIdx = diaryDao.getSenderUserIdx(diaryIdx); // 발신인 userIdx

            for (User user : userList_total) {
                int userIdx = user.getUserIdx();
                if (userIdx != senderUserIdx) {
                    diaryDao.setDiarySendList(diaryIdx, userIdx); // 일기 발송
                }
            }
        }

        // ---------------------------- 3. 당일 발송해야 할 일기의 개수가 2개 이상인 경우 -----------------------------

        else {
            // TODO : (1) 비슷한 나이대 발송

            // 2차원 가변 배열
            // [diaryIdxList 인덱스 값][해당 일기 발신인과 비슷한 나이대를 갖는 userIdx]
            List<List<Integer>> userIdxList_similarAge = new ArrayList<>();

            int i = 0;
            for (int diaryIdx : diaryIdxList) {
                userIdxList_similarAge.add(new ArrayList<>());

                int senderUserIdx = diaryDao.getSenderUserIdx(diaryIdx); // 발신인 userIdx
//                System.out.println("senderUserIdx = "+ senderUserIdx);
                int senderBirth = diaryDao.getSenderBirth(diaryIdx); // 발신인 생년
//                System.out.println("senderBirth = "+ senderBirth);
//                System.out.println();

                // 발송 가능한(User.recOthers = 1) & 비슷한 나이대를 갖는(senderBirth -5 ~ +5) & 일기를 같은 사람한테서 연속으로 2번 받지 않는 모든 userIdx
                userIdxList_similarAge.get(i).addAll(diaryDao.getUserIdxList_similarAge(senderUserIdx, senderBirth)); // 발신인 본인 제외
                i++;
            }

            int userIdxNum_similarAge = diaryDao.getUserIdxNum_similarAge(); // 비슷한 나이대 수신 동의한 회원 수 (User.recSimilarAge = 1)

            // 비슷한 나이대 수신 동의한 회원 수 중 70 ~ 80%는 비슷한 나이로 수신받게 함
            // 일기마다 비슷한 나이대로 발송할 최소 횟수 = 비슷한 나이대 수신에 동의한 회원 집단 중 실제로 비슷한 나이대로 발송할 최소 회원 수 / 당일 발송해야 하는 일기 개수
            // Constant.DIARY_REC_SIMILAR_AGE_RATIO = 80
            int sendNum_similarAge = userIdxNum_similarAge * Constant.DIARY_REC_SIMILAR_AGE_RATIO / 100 / diaryIdxList.size(); // 일기마다 비슷한 나이대로 발송할 최소 횟수
//            System.out.println("sendNum_similarAge = " + sendNum_similarAge);

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

                    diaryDao.setDiarySendList(diaryIdx, receiverIdx); // 일기 발송
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

            // TODO : (2) 일반 발송
            // (비슷한 나이대 발송 후 나머지 회원 수 / 총 일기 개수)의 몫만큼 발송

            // List<Integer> userIdxList_total = diaryDao.getUserIdxList_total(); // 수신 동의한 모든 userIdx (User.recOthers = 1)
            // -> 일기를 발송 받아야 하는 userIdx 리스트로 갱신
            List<User> userList_total_updated = new ArrayList<>();
            for (User user : userList_total) {
                int userIdx = user.getUserIdx();
                if (!userIdx_sendMap.get(userIdx)) { // 회원마다 일기 발송 유무 확인 (Map.value = false -> add)
                    userList_total_updated.add(user);
                }
            }
            userList_total = userList_total_updated; // 갱신

            for (User user : userList_total) { // 일기를 발송 받아야 하는 회원 수만큼
                int userIdx = user.getUserIdx();

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

            // TODO : (3) 일반 발송 처리 후 나머지 발송
            // (비슷한 나이대 발송 후 나머지 회원 수 / 총 일기 개수)의 나머지 부분 발송

            userList_total_updated = new ArrayList<>(); // 일기를 발송 받아야 하는 모든 userIdx
            for (User user : userList_total) {
                int userIdx = user.getUserIdx();

                if (!userIdx_sendMap.get(userIdx)) { // 회원마다 일기 발송 유무 확인 (Map.value = false -> add)
                    userList_total_updated.add(user);
                }
            }
            userList_total = userList_total_updated; // 갱신

            if (userList_total.size() != 0) {
                for (User user : userList_total) { // 일기를 발송 받아야 하는 회원 수만큼
                    int userIdx = user.getUserIdx();

                    List<Integer> diaryIdxList_updated = new ArrayList<>(); // 발송해야 하는 diaryIdx 목록
                    for (int diaryIdx : diaryIdxList) { // 당일 발송해야 하는 모든 일기 개수만큼
                        if (diaryIdx_sendNumMap.get(diaryIdx) <= sendNum) { // 일기마다 발송된 횟수 확인 (Map.value <= 발송되어야 하는 최소 횟수 -> add)
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
         * TODO : 알림 테이블에 저장
         */
        diaryIdxList = diaryDao.getDiaryIdxList(yesterday); // 당일 발송해야 하는 모든 diaryIdx

        List<GetSendListRes> diarySendList = new ArrayList<>();

        // 일기에 따른 발송 리스트 조회
        for (int diaryIdx : diaryIdxList) {
            String senderNickName = diaryDao.getSenderNickName(diaryIdx); // 발신인 nickName
            GetSendListRes diary = new GetSendListRes(diaryIdx, senderNickName);
            diary.setReceiverIdxList(diaryDao.getReceiverIdxList(diaryIdx, yesterday));
//            diary.setReceiverIdxList(diaryDao.getReceiverIdxList(diaryIdx, "2022.02.05")); // test
            diarySendList.add(diary);
        }
        alarmService.postAlarm_diary(diarySendList); // 알림 저장

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
            diaryDao.setDiarySendList(diaryIdx, userIdx); // 일기 발송
            userIdx_sendMap.put(userIdx, true); // 일기 발송 유무 변경 : Map.value = false -> true

            int sendNum_diary = diaryIdx_sendNumMap.get(diaryIdx); // 기존 일기 발송 횟수
            diaryIdx_sendNumMap.put(diaryIdx, ++sendNum_diary); // 일기 발송 횟수 갱신 (+1)

            return true;

        } else return false; // userIdx == senderUserIdx인 경우 -> return false (sendDiary_general 함수 다시 실행)

    }

    // ================================================================================================================

    /*
     * 일기 발송 리스트 조회
     * [GET] /diaries/diarySendList
     */
    public List<GetSendListRes> getDiarySendList() throws BaseException {
        try {
            List<GetSendListRes> result = new ArrayList<>();

            String yesterday = LocalDate.now().minusDays(1).toString().replaceAll("-", "."); // 어제 날짜 (yyyy.MM.dd)
            List<Integer> diaryIdxList = diaryDao.getDiaryIdxList(yesterday); // 당일 발송해야 하는 모든 diaryIdx
//            List<Integer> diaryIdxList = diaryDao.getDiaryIdxList("2022.03.17"); // test

            if (diaryIdxList.size() == 0) {
                throw new BaseException(NO_DIARY_SENT_TODAY); // 오늘 발송되는 일기는 없습니다.
            }

            // 일기에 따른 발송 리스트 조회
            for (int diaryIdx : diaryIdxList) {
                String senderNickName = diaryDao.getSenderNickName(diaryIdx); // 발신인 nickName
                GetSendListRes diary = new GetSendListRes(diaryIdx, senderNickName);
                diary.setReceiverIdxList(diaryDao.getReceiverIdxList(diaryIdx, yesterday));
//                diary.setReceiverIdxList(diaryDao.getReceiverIdxList(diaryIdx, "2022.02.05")); // test
                result.add(diary);
            }

            return result;

        } catch (BaseException exception) {
            throw new BaseException(NO_DIARY_SENT_TODAY); // 오늘 발송되는 일기는 없습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================================================

    /*
     * 현재 서버 시간 확인
     * [GET] /diaries/test
     */
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
