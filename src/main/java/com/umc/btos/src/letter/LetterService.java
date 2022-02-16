package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
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

            // 1. 편지 저장
            int letterIdx = letterDao.postLetter(postLetterReq);
            String senderNickName = letterDao.getNickName(senderUserIdx); // 발신인 닉네임

            // 2. 편지 발송
            List<Receiver> receiverList = new ArrayList<>(); // ArrayList : 데이터 검색에 유리
            List<Integer> receiverIdxList = new ArrayList<>(); // 편지 발송받은 회원 목록
            // 5명 뽑기 (본인 제외)
            // 3명은 비슷한 나이대에서 전송
            int totalNum = 5;
            int similarNum = 3;
            int leftNum = totalNum - similarNum;
            // 나머지 2명은 랜덤
            // 바로 이전에 발송받았던 회원에게서는 최대한 편지를 받지 못하도록 하자
            // Map보단 DTO 사용?

            // 보낼 후보 userIdxList - 본인 제외
            List<User> userList = letterDao.getUserList(senderUserIdx); // 편지 발송 가능한 회원들의 목록 (본인 제외)
            System.out.println("userList.size() = " + userList.size());
            Map<Integer, Boolean> userIdx_sendMap = new HashMap<>(); // key = userIdx, value = 편지 발송 유무 (발송되었다면 true, 아직 안 되었다면 false)
            for (User user : userList) {
                userIdx_sendMap.put(user.getUserIdx(), false); // 편지 발송 가능한 회원마다 저장 공간 생성
            }

            // 가장 최근에 수신한 편지의 발신인 userIdx 저장
            for (User user : userList) {
                // 편지 하나도 발송받지 못한 유저라면?
                if (letterDao.hasReceivedLetter(user.getUserIdx()) == 1) {
                    int userIdx_recentReceived = letterDao.getUserIdx_recentReceived(user.getUserIdx());
                    user.setUserIdx_recentReceived(userIdx_recentReceived);
                } else {
                    user.setUserIdx_recentReceived(0);
                }
            }

            // 비슷한 나이대에 해당되는 회원 목록 생성
            // recSimilarAge = 1
            // 비슷한 나이대 : 발신인의 나이 -5 ~ +5
            // 가장 최근에 수신한 편지 != 발신인의 편지
            List<User> userList_similarAge = new LinkedList<>();
            int senderBirth = letterDao.getSenderBirth(senderUserIdx); // 발신인 생년
            System.out.println(senderBirth);
            int similarAge_min = senderBirth - 5; // 비슷한 나이대로 불러올 최소 생년
            int similarAge_max = senderBirth + 5; // 비슷한 나이대로 불러올 최대 생년

            // 발신인이 생년을 입력하지 않은 경우?
            if (senderBirth != 0) {
                for (User user : userList) {
                    if (user.getUserIdx_recentReceived() != senderUserIdx) {
                        int userBirth = user.getBirth();
                        if (user.getRecSimilarAge() == 1 && userBirth >= similarAge_min && userBirth <= similarAge_max) {
                            userList_similarAge.add(user);
                        }
                    }
                }

                int userNum_similarAge = userList_similarAge.size();
                System.out.println("userNum_similarAge = " + userNum_similarAge);
                if (userNum_similarAge <= 3) {
                    // 전부 발송 ㄱㄱ
                    for (User user : userList_similarAge) {
                        int userIdx = user.getUserIdx();
                        letterDao.sendLetter(letterIdx, userIdx); // 편지 발송
                        userIdx_sendMap.put(userIdx, true); // Map.value = false -> true (해당 회원에게 편지가 발송됨을 체크)
                        receiverIdxList.add(userIdx);
                    }
                    leftNum = totalNum - userNum_similarAge;

                } else { // userNum_similarAge > 3
                    // 3명 랜덤으로 뽑기
                    for (int i = 0; i < 3; i++) {
                        int idx = (int) (Math.random() * userList_similarAge.size());

                        System.out.println(userList_similarAge.get(idx).getUserIdx());
                        int userIdx = userList_similarAge.get(idx).getUserIdx();
                        letterDao.sendLetter(letterIdx, userIdx); // 편지 발송
                        userIdx_sendMap.put(userIdx, true); // Map.value = false -> true (해당 회원에게 편지가 발송됨을 체크)
                        receiverIdxList.add(userIdx);
                        userList_similarAge.remove(idx); // 발송 후 목록에서 제거
                    }
                }
                System.out.println();
            } else {
                leftNum = 5;
            }

            // leftNum만큼 랜덤으로 뽑기
            List<User> userList_random = new LinkedList<>();
            for (User user : userList) {
                if (user.getUserIdx_recentReceived() != senderUserIdx && !userIdx_sendMap.get(user.getUserIdx())) { // 가장 최근에 수신한 편지 != 발신인의 편지, 편지 아직 발송 안된 사람 중
                    userList_random.add(user);
                }
            }

            if (userList_random.size() < leftNum) {
                leftNum = userList_random.size();
            }

            for (int i = 0; i < leftNum; i++) {
                int idx = (int) (Math.random() * userList_random.size());

                int userIdx = userList_random.get(idx).getUserIdx();
                System.out.println(userIdx);

                letterDao.sendLetter(letterIdx, userIdx); // 편지 발송
                userIdx_sendMap.put(userIdx, true); // Map.value = false -> true (해당 회원에게 편지가 발송됨을 체크)
                receiverIdxList.add(userIdx);
                userList_random.remove(idx); // 발송 후 목록에서 제거
            }
            System.out.println();

            for (int userIdx : receiverIdxList) {
                Receiver receiver = new Receiver(userIdx);
                receiver.setFcmToken(letterDao.getFcmToken(userIdx));
                receiverList.add(receiver);
            }

            return new PostLetterRes(letterIdx, senderNickName, receiverList);

        } catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 편지 삭제 - status를 deleted로 변경 (Patch)
    public void modifyLetterStatus(PatchLetterReq patchLetterReq) throws BaseException {
        try {
            // result값이 0이면 과정이 실패 - 8001 ERROR
            int result = letterDao.modifyLetterStatus(patchLetterReq);
            if (result == 0) throw new BaseException(MODIFY_FAIL_LETTER_STATUS);

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
