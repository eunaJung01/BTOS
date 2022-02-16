package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.Constant;
import com.umc.btos.src.letter.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class LetterService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LetterDao letterDao;
    private final LetterProvider letterProvider;

    @Autowired
    public LetterService(LetterDao letterDao, LetterProvider letterProvider) {
        this.letterDao = letterDao;
        this.letterProvider = letterProvider;
    }

    /*
     * 편지 저장 및 발송
     * [POST] /letters
     */
    public PostLetterRes postLetter(PostLetterReq postLetterReq) throws BaseException {
        try {
            int senderUserIdx = postLetterReq.getUserIdx(); // 발신인 userIdx

            // 1. 편지 저장 (INSERT Letter)
            int letterIdx = letterDao.postLetter(postLetterReq);
            String senderNickName = letterDao.getNickName(senderUserIdx); // 발신인 닉네임

            // 2. 편지 발송 (INSERT LetterSendList)
            List<Receiver> receiverList = new ArrayList<>(); // Receiver 객체 - 해당 편지를 발송받은 회원의 정보 저장 (userIdx, fcmToken)
            List<Integer> receiverIdxList = new ArrayList<>(); // 해당 편지를 발송받은 회원들의 목록 (userIdx)

            /*
             * 총 5명에게 발송
             *
             * 조건
             *      1. 본인 제외
             *      2. 가장 최근에 발신인의 편지를 받은 회원 제외 (연속해서 같은 회원의 편지를 발송받지 않도록 하기 위함)
             *      3. 비슷한 나이대 : 비슷한 나이대로 수신에 동의한 회원들(User.recSimilarAge = 1) 중 발신인의 생년 -5 ~ +5
             *
             * 발송 비율
             *      1. 기본
             *          3명 - 비슷한 나이대 회원들의 집단에서 발송
             *          2명 - 비슷한 나이대 회원들을 제외한 집단에서 무작위로 발송
             *
             *      2. 비슷한 나이대의 회원이 3명 이하(n)인 경우
             *          n명 - 비슷한 나이대 회원들의 집단에서 발송
             *          5-n명 - 비슷한 나이대 회원들을 제외한 집단에서 무작위로 발송
             *
             *      3. 가장 최근에 발신인의 편지를 받은 회원들을 제외하였을 때 발송 가능한 인원수가 5명 미만(n)인 경우
             *          비슷한 나이대의 회원 수 : m (m <= n < 5)
             *          m명 - 비슷한 나이대 회원들의 집단에서 발송
             *          n-m명 - 비슷한 나이대 회원들을 제외한 집단에서 무작위로 발송
             *
             *          회원의 수가 적고 편지 작성 및 발송을 특정 회원만 계속 한다면 후에는 발송 가능한 회원이 없을 수 있다는 문제 발생
             *          -> 초기 유저 확보가 중요!
             *
             *      4. 발신인의 생년 정보가 없는 경우
             *          5명 - 편지 발송이 가능한 전체 집단에서 무작위로 발송
             */

            int totalNum = Constant.LETTER_SEND_TOTAL_NUM; // 편지를 발송해야 하는 총 횟수 (5회)
            int similarNum = Constant.LETTER_SEND_SIMILAR_AGE_NUM; // 비슷한 나이대의 회원들에게 편지를 발송할 횟수 (3회)
            int leftNum = totalNum - similarNum; // 비슷한 나이대 회원들을 제외한 집단에서 무작위로 편지를 발송해야 하는 횟수

            List<User> userList = letterDao.getUserList(senderUserIdx); // 편지 발송 가능한 회원들의 목록 (본인 제외)
//            System.out.println("userList.size() = " + userList.size());
            Map<Integer, Boolean> userIdx_sendMap = new HashMap<>(); // key = userIdx, value = 편지 발송 유무 (발송되었다면 true, 아직 안 되었다면 false)
            for (User user : userList) {
                userIdx_sendMap.put(user.getUserIdx(), false); // 편지 발송이 가능한 회원마다 저장 공간 생성
            }

            // 가장 최근에 수신한 편지의 발신인 userIdx 저장
            for (User user : userList) {
                int userIdx = user.getUserIdx();

                // 해당 회원이 편지를 발송받은 적이 있는지 확인
                if (letterDao.hasReceivedLetter(userIdx) == 1) { // 편지를 발송받은 적이 있는 경우
                    int userIdx_recentReceived = letterDao.getUserIdx_recentReceived(userIdx);
                    user.setUserIdx_recentReceived(userIdx_recentReceived);
                } else { // 편지를 한번도 발송받지 못한 경우
                    user.setUserIdx_recentReceived(0);
                }
            }

            // 비슷한 나이대에 해당되는 회원 목록 생성
            List<User> userList_similarAge = new LinkedList<>();
            int senderBirth = letterDao.getSenderBirth(senderUserIdx); // 발신인 생년 (User.birth = null인 경우 0으로 저장)

            // 발신인 생년 정보가 존재하는 경우
            if (senderBirth != 0) {
                // 비슷한 나이대 : 발신인의 생년 -5 ~ +5
                int similarAge_min = senderBirth - Constant.SIMILAR_AGE_STANDARD; // 비슷한 나이대로 불러올 최소 생년
                int similarAge_max = senderBirth + Constant.SIMILAR_AGE_STANDARD; // 비슷한 나이대로 불러올 최대 생년

                for (User user : userList) {
                    if (user.getUserIdx_recentReceived() != senderUserIdx) { // 가장 최근에 수신한 편지 != 발신인의 편지
                        int userBirth = user.getBirth(); // 회원의 생년
                        if (user.getRecSimilarAge() == 1 && userBirth >= similarAge_min && userBirth <= similarAge_max) {
                            userList_similarAge.add(user);
                        }
                    }
                }

                int userNum_similarAge = userList_similarAge.size(); // 비슷한 나이대에 해당되는 회원들의 수
//                System.out.println("userNum_similarAge = " + userNum_similarAge);

                // 발송 비율 2. 비슷한 나이대의 회원이 3명 이하(n)인 경우 -> n명 전부 발송
                if (userNum_similarAge <= 3) {
                    for (User user : userList_similarAge) {
                        int userIdx = user.getUserIdx();
                        letterDao.sendLetter(letterIdx, userIdx); // 편지 발송

                        userIdx_sendMap.put(userIdx, true); // Map.value = false -> true (해당 회원에게 편지가 발송됨을 체크)
                        receiverIdxList.add(userIdx);
                    }
                    leftNum = totalNum - userNum_similarAge; // 무작위로 편지를 발송해야 하는 횟수 갱신
                }

                // 발송 비율 1. 기본 -> 비슷한 나이대의 회원 집단에서 3명 무작위로 발송
                else { // userNum_similarAge > 3
                    for (int i = 0; i < 3; i++) {
                        int idx = (int) (Math.random() * userList_similarAge.size());
//                        System.out.println(userList_similarAge.get(idx).getUserIdx());
                        int userIdx = userList_similarAge.get(idx).getUserIdx();
                        letterDao.sendLetter(letterIdx, userIdx); // 편지 발송

                        userIdx_sendMap.put(userIdx, true); // Map.value = false -> true (해당 회원에게 편지가 발송됨을 체크)
                        receiverIdxList.add(userIdx);
                        userList_similarAge.remove(idx); // 발송 후 목록에서 제거
                    }
                }
//                System.out.println();
            }

            // 발송 비율 4. 발신인의 생년 정보가 없는 경우 -> 편지 발송이 가능한 전체 집단에서 무작위로 발송
            else {
                leftNum = 5; // 무작위로 편지를 발송해야 하는 횟수 갱신
            }

            // leftNum 수만큼 무작위로 발송
            List<User> userList_random = new LinkedList<>(); // 편지 발송 가능한 회원들 목록
            for (User user : userList) {
                if (user.getUserIdx_recentReceived() != senderUserIdx && !userIdx_sendMap.get(user.getUserIdx())) {
                    userList_random.add(user);
                }
            }

            // 편지 발송 가능한 회원의 수(n)가 총 편지 발송 수(totalNum)을 채우기 위해 필요한 회원 수보다 적을 경우
            // -> n명 전부 발송
            if (userList_random.size() < leftNum) {
                leftNum = userList_random.size();
            }

            for (int i = 0; i < leftNum; i++) {
                int idx = (int) (Math.random() * userList_random.size());
                int userIdx = userList_random.get(idx).getUserIdx();
//                System.out.println(userIdx);
                letterDao.sendLetter(letterIdx, userIdx); // 편지 발송

                userIdx_sendMap.put(userIdx, true); // Map.value = false -> true (해당 회원에게 편지가 발송됨을 체크)
                receiverIdxList.add(userIdx);
                userList_random.remove(idx); // 발송 후 목록에서 제거
            }
//            System.out.println();

            // set receiverList
            for (int userIdx : receiverIdxList) {
                Receiver receiver = new Receiver(userIdx);
                receiver.setFcmToken(letterDao.getFcmToken(userIdx));
                receiverList.add(receiver);
            }

            return new PostLetterRes(letterIdx, senderNickName, receiverList);

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 편지 삭제
     * [PATCH] /letters/:letterIdx
     */
    public void deleteLetter(int letterIdx) throws BaseException {
        try {
            if (letterDao.deleteLetter(letterIdx) == 0) {
                throw new BaseException(MODIFY_FAIL_LETTER_STATUS); // 편지 삭제에 실패하였습니다.
            }

        } catch (BaseException exception) {
            throw new BaseException(MODIFY_FAIL_LETTER_STATUS); // 편지 삭제에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 편지 작성(POST)
    /*
    public PostLetterRes createLetter(PostLetterReq postLetterReq) throws BaseException {
        try {
            //letter 테이블에 편지 생성
            // 생성한 편지의 letterIdx반환
            int letterIdx = letterDao.createLetter(postLetterReq);

            // 생성한 유저의 idx와 SimilarAge여부를 반환
            PostLetterUserSimilarIdx idx_similar = letterDao.getIdx_Similar(letterIdx);

            // [또래 유저 발송 O]
            // 편지 발송 유저가 또래 편지 수신을 원할경우
            if (idx_similar.getUserSimilarAge() == 1) {
                // 랜덤으로 선택된 5명의 또래 유저Idx 리스트
                List<Integer> receiveUserIdx_similar = letterDao.getLetterUserIdx_Similar(idx_similar);

                //letterSendList 테이블에 발송 편지 목록 추가
                //(최대 5명) 뽑힌 유저의 수만큼 편지 발송 // 1명씩 테이블에 추가하므로 5번 반복
                for (int i = 0; i < receiveUserIdx_similar.size(); i++) {
                    letterDao.createLetterSendList(letterIdx, receiveUserIdx_similar, i);
                }

                // 또래의 유저가 5명 미만이라면
                if (receiveUserIdx_similar.size() < 5) {
                    Map<Integer, Integer> letterIdxMap = new HashMap<>();
                    // Key :  이미 편지를 수신받은 유저Idx, Value : 1
                    for (int pastSend : receiveUserIdx_similar) {
                        letterIdxMap.put(pastSend, 1);
                    }

                    // 랜덤으로 보낼 유저 인덱스의 리스트 생성
                    // 이미 편지를 보낸 유저가 존재할 수 있으므로 넉넉하게 10명 뽑음
                    List<Integer> receiveUserIdx_Random = letterDao.getLetterUserIdx_Random(idx_similar);

                    // (5 - 편지 수신 완료 User의 수) 만큼 랜덤으로 선택한 유저에게 편지 발송
                    int sendUserIdx = 0;
                    int similar_size = receiveUserIdx_similar.size();
                    // j번(5 - 편지 수신 완료 User의 수) 만큼 편지 전송
                    for (int j = 0; j < (5 - similar_size); j++, sendUserIdx++) {
                        int userIdx = receiveUserIdx_Random.get(sendUserIdx);

                        // 만약 이미 편지를 받은 유저라면
                        if (letterIdxMap.containsKey(userIdx) == true) {
                            j -= 1; // 편지를 보낸 수 줄임
                            continue;
                        }

                        // LetterSendList 테이블에 편지 발송 추가
                        letterDao.createLetterSendList(letterIdx, receiveUserIdx_Random, sendUserIdx);
                        // 편지 발송 목록에 해당 userIdx 추가  // 편지 발송 API의 Res에 필요
                        receiveUserIdx_similar.add(userIdx);
                    }
                }
                // 화분 점수 증가
                PatchModifyScoreRes ModifyScore = plantService.modifyScore_plus(postLetterReq.getUserIdx(), Constant.PLANT_LEVELUP_LETTER, "letter");
                // 편지 발송 유저의 닉네임
                String senderNickName = getNickName(postLetterReq.getUserIdx());
                PostLetterRes result_similar = new PostLetterRes(letterIdx, senderNickName, receiveUserIdx_similar, ModifyScore);
                return result_similar;
            }

            // [또래 유저 발송 X]
            else {
                // 랜덤으로 선택된 5명의 유저Idx 리스트
                List<Integer> receiveUserIdx = letterDao.getLetterUserIdx(idx_similar);

                //letterSendList 테이블에 발송 편지 목록 추가
                //(최대 5명) 뽑힌 유저의 수만큼 편지 발송 // 1명씩 테이블에 추가하므로 5번 반복
                // 만약 5명보다 적을경우(유저가 5명이 안될경우)에도 선택된 유저만큼만 발송되므로 ERROR발생 X
                for (int i = 0; i < receiveUserIdx.size(); i++) {
                    letterDao.createLetterSendList(letterIdx, receiveUserIdx, i);
                }

                // 편지 발송 유저의 닉네임
                String senderNickName = getNickName(postLetterReq.getUserIdx());
                PostLetterRes result = new PostLetterRes(letterIdx, senderNickName, receiveUserIdx, ModifyScore);
                return result;
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
     */

}
