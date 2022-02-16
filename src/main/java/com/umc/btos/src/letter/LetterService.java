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

    // ================================================================================================================

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

    // ================================================================================================================

    /*
     * 편지 삭제
     * [PATCH] /letters/:letterIdx
     */
    public void deleteLetter(int letterIdx) throws BaseException {
        if (letterDao.deleteLetter(letterIdx) == 0) {
            throw new BaseException(MODIFY_FAIL_LETTER_STATUS); // 편지 삭제에 실패하였습니다.
        }
    }

}
