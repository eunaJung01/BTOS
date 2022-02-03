package com.umc.btos.src.diary;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.umc.btos.config.*;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.diary.model.*;
import com.umc.btos.utils.AES128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 일기 조회
     */
    public GetDiaryRes getDiary(int diaryIdx) throws BaseException {
        try {
            GetDiaryRes diary = diaryDao.getDiary(diaryIdx); // 일기의 정보
            diary.setDoneList(diaryDao.getDoneList(diaryIdx)); // done list 정보

            // content 복호화
            if (diary.getIsPublic() == 0) { // private 일기일 경우 content 복호화
                decryptContents(diary);
            }
            return diary;

        } catch (Exception exception) {
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
            }

        } catch (Exception ignored) {
            throw new BaseException(DIARY_DECRYPTION_ERROR); // 일기 복호화에 실패하였습니다.
        }
    }

    /*
     * 일기 발송
     * [GET] /diaries/diarySendList
     */
    public GetDiarySendListRes getDiarySendList() throws BaseException {
        try {
            // 발송 구현 (이 부분은 Spring Scheduler로 매일 오후 18시 59분(?)에 되어야 함)
            GetDiarySendListRes result = new GetDiarySendListRes();
            List<DiarySendList> diarySendList = new ArrayList<>();

            LocalDate now = LocalDate.now(); // 오늘 날짜 (yyyy-MM-dd)
//            List<Integer> diaryIdxList = diaryDao.getDiaryIdxList(now.toString()); // 당일 발송해야 하는 모든 diaryIdx
            List<Integer> diaryIdxList = diaryDao.getDiaryIdxList("2022-02-01"); // test
            System.out.println(diaryIdxList);
            if (diaryIdxList.size() == 0) {
//                return false; // 알고리즘 종료
                return result;
            }

            Map<Integer, Integer> diaryIdx_sendNumMap = new HashMap<>(); // <diaryIdx, 해당 일기가 발송된 횟수> 일기마다 현재까지 몇명에게 보내졌는가?
            for (int diaryIdx : diaryIdxList) {
                diaryIdx_sendNumMap.put(diaryIdx, 0);
            }

            List<Integer> userIdxList_total = diaryDao.getUserIdxList_total(); // 일기를 발송받을 모든 userIdx
            System.out.println(userIdxList_total);

            Map<Integer, Boolean> userIdx_sendMap = new HashMap<>(); // <userIdx, 해당 회원에게 일기 발송 유무>
            for (int userIdx : userIdxList_total) {
                userIdx_sendMap.put(userIdx, false);
            }
            int totalUserNum = userIdxList_total.size(); // 총 인원수

            // 일기마다 보내져야 하는 횟수 - (sendNum ~ sendNum+1)번 보내져야 함
            int sendNum = totalUserNum / diaryIdxList.size(); // 총 인원수 / 총 일기 개수
            System.out.println(sendNum);
            // sendNum = 1인 경우? -> 총 인원수만큼 diaryIdx 랜덤 돌려서 비슷한 나이대 리스트에서 1명한테 보내셈


            // --------------------------------------- 비슷한 나이대 발송 처리 ---------------------------------------

            List<List<Integer>> userIdxList_similarAge = new ArrayList<>(); // [diaryIdxList 인덱스 값][비슷한 나이대를 갖는 userIdx]
            int i = 0;
            for (int diaryIdx : diaryIdxList) {
                userIdxList_similarAge.add(new ArrayList<>());

                int senderUserIdx = diaryDao.getSenderUserIdx(diaryIdx); // 발신인 userIdx
                int senderBirth = diaryDao.getSenderBirth(diaryIdx); // 발신인 생년

                // 발송 가능한 & 비슷한 나이대를 갖는(senderBirth -5 ~ +5) 모든 userIdx
                userIdxList_similarAge.get(i).addAll(diaryDao.getUserIdxList_similarAge(senderUserIdx, senderBirth)); // 발신인 userIdx 제외
                System.out.println("userIdxList_similarAge = " + diaryDao.getUserIdxList_similarAge(senderUserIdx, senderBirth));
                i++;
            }

            // 일기 발송받을 전체 회원들 중 몇명이 같은 나이로 수신 == 1일까? -> 그 사람들의 60~70퍼는 같은 나이로 수신받았으면 좋겠음
            int userIdxNum_similarAge = diaryDao.getUserIdxNum_similarAge();

            // 일기마다 비슷한 나이대로 보낼 횟수 : '비슷한 나이대로 보낼 총 회원 수 / 일기 개수(일기마다 동일한 숫자로 보내주기 위해서)'
            int sendNum_similarAge = (int) (userIdxNum_similarAge * 0.65 / diaryIdxList.size());
            System.out.println("sendNum_similarAge = " + sendNum_similarAge);

            int j = 0;
            for (int diaryIdx : diaryIdxList) { // 당일 발송해야 하는 일기 개수만큼
                int userNum_similarAge = userIdxList_similarAge.get(j).size(); // 비슷한 나이대를 갖는 회원들의 수 (최초 저장된 값)
                System.out.println("userNumSimilarAge = " + userNum_similarAge);

                // userIdxList_similarAge에서 map value = true인 것들 지우기 -> 이번에 발송할 후보에 드는 사람들 리스트
                List<Integer> userIdxList_similarAge_updated = new ArrayList<>();
                for (int k = 0; k < userNum_similarAge; k++) {
                    int userIdx = userIdxList_similarAge.get(j).get(k);
                    if (!userIdx_sendMap.get(userIdx)) { // Map.value = false -> add
                        userIdxList_similarAge_updated.add(userIdx);
                    }
                }

                for (int k = 0; k < sendNum_similarAge; k++) {
                    int idx = (int) (Math.random() * userIdxList_similarAge_updated.size()); // 0 ~ 마지막 인덱스 랜덤으로 반환
                    System.out.println("idx = " + idx);

                    int receiverIdx = userIdxList_similarAge_updated.get(idx); // 수신자 userIdx
                    System.out.println("receiverIdx = " + receiverIdx);

                    diaryDao.setDiarySendList(diaryIdx, receiverIdx); // INSERT INTO DiarySendList Table
                    userIdx_sendMap.put(receiverIdx, true); // Map.value = false -> true

                    for (int l = 0; l < userIdxList_similarAge_updated.size(); l++) {
                        if (userIdxList_similarAge_updated.get(l) == receiverIdx) {
                            userIdxList_similarAge_updated.remove(l);
                        }
                    }
                }
                diaryIdx_sendNumMap.put(diaryIdx, sendNum_similarAge); // 일기 보내진 횟수 저장
                j++;
            }

            System.out.println();

            // --------------------------------------- 일반 발송 처리 (나머지) ---------------------------------------
            
            // userIdxList_similarAge에서 map value = true인 것들 지우기 -> 갱신
            List<Integer> userIdxList_total_updated = new ArrayList<>(); // 이 사람들 내에서 기존 일기 발송 알고리즘 구현
            for (int userIdx : userIdxList_total) {
                if (!userIdx_sendMap.get(userIdx)) { // Map.value = false -> add
                    userIdxList_total_updated.add(userIdx);
                }
            }
            userIdxList_total = userIdxList_total_updated; // 갱신

            for (int userIdx : userIdxList_total) {
                List<Integer> diaryIdxList_updated = new ArrayList<>(); // 발송해야 하는 일기 목록
                for (int diaryIdx : diaryIdxList) {
                    if (diaryIdx_sendNumMap.get(diaryIdx) < sendNum) { // Map.value = false -> add
                        diaryIdxList_updated.add(diaryIdx);
                    }
                }

                if (diaryIdxList_updated.size() != 0) { // 나머지가 있는 경우 제외
                    sendDiary(diaryIdxList_updated, diaryIdx_sendNumMap, userIdx_sendMap, userIdx, diaryIdxList_updated);
                }
            }

            System.out.println();

            // 나머지 부분들 처리
            // userIdxList_similarAge에서 map value = true인 것들 지우기 -> 갱신
            userIdxList_total_updated = new ArrayList<>(); // ㄹㅇ 나머지 사람들
            for (int userIdx : userIdxList_total) {
                if (!userIdx_sendMap.get(userIdx)) { // Map.value = false -> add
                    userIdxList_total_updated.add(userIdx);
                }
            }
            userIdxList_total = userIdxList_total_updated; // 갱신
            System.out.println("userIdxList_total = " + userIdxList_total);

            if (userIdxList_total.size() != 0) {
                for (int userIdx : userIdxList_total) {
                    List<Integer> diaryIdxList_updated = new ArrayList<>(); // 발송해야 하는 일기 목록
                    for (int diaryIdx : diaryIdxList) {
                        if (diaryIdx_sendNumMap.get(diaryIdx) == sendNum) { // Map.value = false -> add
                            diaryIdxList_updated.add(diaryIdx);
                        }
                    }
                    System.out.println("diaryIdxList_updated = " + diaryIdxList_updated);

                    sendDiary(diaryIdxList, diaryIdx_sendNumMap, userIdx_sendMap, userIdx, diaryIdxList_updated);
                }
            }

            return result;

        } catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 일기 발송 (비슷한 나이대로 수신한 이후)
    private void sendDiary(List<Integer> diaryIdxList, Map<Integer, Integer> diaryIdx_sendNumMap, Map<Integer, Boolean> userIdx_sendMap, int userIdx, List<Integer> diaryIdxList_updated) {
        int diaryIdx = diaryIdxList_updated.get((int) (Math.random() * diaryIdxList.size())); // 0 ~ 마지막 인덱스 랜덤으로 반환
        System.out.println("diaryIdx = " + diaryIdx);

        int senderUserIdx = diaryDao.getSenderUserIdx(diaryIdx); // 발신인 userIdx
        System.out.println("receiverIdx = " + userIdx);

        if (userIdx != senderUserIdx) {
            diaryDao.setDiarySendList(diaryIdx, userIdx); // INSERT INTO DiarySendList Table
            userIdx_sendMap.put(userIdx, true); // Map.value = false -> true

            int sendNum_diary = diaryIdx_sendNumMap.get(diaryIdx); // 일기 보내진 횟수
            diaryIdx_sendNumMap.put(diaryIdx, ++sendNum_diary); // 일기 보내진 횟수 갱신
        }
    }

}
