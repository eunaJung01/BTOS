package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.letter.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // 편지 작성(POST)
//    public PostLetterRes createLetter(PostLetterReq postLetterReq) throws BaseException {
//        try {
//            //letter 테이블에 편지 생성
//            // 생성한 편지의 letterIdx반환
//            int letterIdx = letterDao.createLetter(postLetterReq);
//
//            // 생성한 유저의 idx와 SimilarAge여부를 반환
//            PostLetterUserSimilarIdx idx_similar = letterDao.getIdx_Similar(letterIdx);
//
//            // [또래 유저 발송 O]
//            // 편지 발송 유저가 또래 편지 수신을 원할경우
//            if (idx_similar.getUserSimilarAge() == 1) {
//                // 랜덤으로 선택된 5명의 또래 유저Idx 리스트
//                List<Integer> receiveUserIdx_similar = letterDao.getLetterUserIdx_Similar(idx_similar);
//
//                //letterSendList 테이블에 발송 편지 목록 추가
//                //(최대 5명) 뽑힌 유저의 수만큼 편지 발송 // 1명씩 테이블에 추가하므로 5번 반복
//                for (int i = 0; i < receiveUserIdx_similar.size(); i++) {
//                    letterDao.createLetterSendList(letterIdx, receiveUserIdx_similar, i);
//                }
//
//                // 또래의 유저가 5명 미만이라면
//                if (receiveUserIdx_similar.size() < 5) {
//                    Map<Integer, Integer> letterIdxMap = new HashMap<>();
//                    // Key :  이미 편지를 수신받은 유저Idx, Value : 1
//                    for (int pastSend : receiveUserIdx_similar) {
//                        letterIdxMap.put(pastSend, 1);
//                    }
//
//                    // 랜덤으로 보낼 유저 인덱스의 리스트 생성
//                    // 이미 편지를 보낸 유저가 존재할 수 있으므로 넉넉하게 10명 뽑음
//                    List<Integer> receiveUserIdx_Random = letterDao.getLetterUserIdx_Random(idx_similar);
//
//                    // (5 - 편지 수신 완료 User의 수) 만큼 랜덤으로 선택한 유저에게 편지 발송
//                    int sendUserIdx = 0;
//                    int similar_size = receiveUserIdx_similar.size();
//                    // j번(5 - 편지 수신 완료 User의 수) 만큼 편지 전송
//                    for (int j = 0; j < (5 - similar_size); j++, sendUserIdx++) {
//                        int userIdx = receiveUserIdx_Random.get(sendUserIdx);
//
//                        // 만약 이미 편지를 받은 유저라면
//                        if (letterIdxMap.containsKey(userIdx) == true) {
//                            j -= 1; // 편지를 보낸 수 줄임
//                            continue;
//                        }
//
//                        // LetterSendList 테이블에 편지 발송 추가
//                        letterDao.createLetterSendList(letterIdx, receiveUserIdx_Random, sendUserIdx);
//                        // 편지 발송 목록에 해당 userIdx 추가  // 편지 발송 API의 Res에 필요
//                        receiveUserIdx_similar.add(userIdx);
//                    }
//                }
//                // 화분 점수 증가
//                PatchModifyScoreRes ModifyScore = plantService.modifyScore_plus(postLetterReq.getUserIdx(), Constant.PLANT_LEVELUP_LETTER, "letter");
//                // 편지 발송 유저의 닉네임
//                String senderNickName = getNickName(postLetterReq.getUserIdx());
//                PostLetterRes result_similar = new PostLetterRes(letterIdx, senderNickName, receiveUserIdx_similar, ModifyScore);
//                return result_similar;
//            }
//
//            // [또래 유저 발송 X]
//            else {
//                // 랜덤으로 선택된 5명의 유저Idx 리스트
//                List<Integer> receiveUserIdx = letterDao.getLetterUserIdx(idx_similar);
//
//                //letterSendList 테이블에 발송 편지 목록 추가
//                //(최대 5명) 뽑힌 유저의 수만큼 편지 발송 // 1명씩 테이블에 추가하므로 5번 반복
//                // 만약 5명보다 적을경우(유저가 5명이 안될경우)에도 선택된 유저만큼만 발송되므로 ERROR발생 X
//                for (int i = 0; i < receiveUserIdx.size(); i++) {
//                    letterDao.createLetterSendList(letterIdx, receiveUserIdx, i);
//                }
//
//                // 편지 발송 유저의 닉네임
//                String senderNickName = getNickName(postLetterReq.getUserIdx());
//                PostLetterRes result = new PostLetterRes(letterIdx, senderNickName, receiveUserIdx, ModifyScore);
//                return result;
//            }
//        } catch (Exception exception) {
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

    /*
     * 편지 저장 및 발송
     * [POST] /letters
     */
    public PostLetterRes postLetter(PostLetterReq postLetterReq) throws BaseException {
        try {


        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    // 편지 발송 유저의 닉네임 반환
    public String getNickName(int userIdx) throws BaseException {
        try {
            String nickName = letterDao.getNickName(userIdx);
            return nickName;
        } catch (Exception exception) {
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

}
