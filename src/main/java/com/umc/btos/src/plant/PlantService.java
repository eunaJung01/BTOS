package com.umc.btos.src.plant;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponseStatus;
import com.umc.btos.config.Constant;
import com.umc.btos.src.plant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.sound.midi.Patch;

import static com.umc.btos.config.BaseResponseStatus.*;
import static com.umc.btos.config.Constant.*;

@Service
public class PlantService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PlantDao plantDao;
    private final PlantProvider plantProvider;

    @Autowired
    public PlantService(PlantDao plantDao, PlantProvider plantProvider) {
        this.plantDao = plantDao;
        this.plantProvider = plantProvider;
    }


    //화분 선택 API ~ 화분의 status를 active로 바꾸는 함수 (selected -> active)
    public void activePlant(int userIdx) throws BaseException {
        try {
            plantDao.activePlant(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    //화분 선택 API
    public BaseResponseStatus selectPlant(PatchSelectPlantReq patchSelectPlantReq) throws BaseException {
        try {
            //회원이 plantIdx를 이미 selected된 화분으로 넘겼는지 체크
            int checkRes = plantProvider.checkPlant(patchSelectPlantReq);
            if (checkRes != 3) //넘겼다면
                throw new BaseException(INVALID_IDX_PLANT);

            //기존에 선택되어있던 화분의 status를 active로 바꾸자 (selected -> active)
            activePlant(patchSelectPlantReq.getUserIdx());

            //status: active -> selected
            if (plantDao.selectPlant(patchSelectPlantReq) == 1) //변경 성공시
                return SUCCESS;
            else //변경 실패시
                throw new BaseException(MODIFY_FAIL_STATUS);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    //화분 구매(보유) API
    public BaseResponseStatus buyPlant(PostBuyPlantReq postBuyPlantReq) throws BaseException {
        try {
            if (plantDao.buyPlant(postBuyPlantReq) == 1) //변경 성공시
                return SUCCESS;
            else //구매 실패시
                throw new BaseException(MODIFY_FAIL_BUY_PLANT);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    // =================================== 화분 점수 및 단계 변경 ===================================

    // 화분 점수 증가
    public PatchModifyScoreRes modifyScore_plus(int userIdx, int score, String type) throws BaseException {
        // score : 변경할 점수

        try {
            // 식물 점수 (score) 가져오기
            int plantScore = plantDao.getScore(userIdx);

            // 식물 단계 (level) 가져오기
            int plantLevel = plantDao.getLevel(userIdx);

            // 해당 단계의 성장치 불러오기
            int plantMaxScore = Constant.PLANT_LEVEL[plantLevel]; // 성장치

            PatchModifyScoreRes result = new PatchModifyScoreRes(false, type); // response 객체

            // 최대 단계 & 최대 점수인 경우
            if (plantLevel == plantDao.getMaxLevel(userIdx) && plantScore == plantMaxScore) {
                return result; // 단계 변경 X
            }

            // 점수 += score
            plantScore += score;

            if (plantScore > plantMaxScore) { // 식물 점수가 해당 단계의 성장치를 넘어간다면? -> 단계 변경
                plantLevel++; // 단계 + 1
                plantScore -= plantMaxScore; // 점수 = 변경된 점수 - 성장치

                if (plantDao.setLevel(userIdx, plantLevel) == 0) { // 변경된 단계 반영
                    throw new BaseException(MODIFY_FAIL_LEVEL); // 화분 단계 변경에 실패하였습니다.
                }
                result.setLevelChanged(true); // 단계가 변경되었다는 정보를 response에 넣기
            }

            if (plantDao.setScore(userIdx, plantScore) == 0) { // 변경된 점수 반영
                throw new BaseException(MODIFY_FAIL_SCORE); // 화분 점수 변경에 실패하였습니다.
            }
            result.setStatus("levelUp");

            return result;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 화분 점수 감소
    public PatchModifyScoreRes modifyScore_minus(int userIdx, int score, String type) throws BaseException {
        // score : 변경할 점수
        try {
            PatchModifyScoreRes result = new PatchModifyScoreRes(false, type); // response 객체

            // 프리미엄 계정인가? -> 프리미엄 계정이라면 성장치 감소 X (바로 response 반환)
            if (plantDao.isPremium(userIdx).compareTo("premium") == 0) {
                return result;
            }

            // 식물 점수 (score) 가져오기
            int plantScore = plantDao.getScore(userIdx);

            // 식물 단계 (level) 가져오기
            int plantLevel = plantDao.getLevel(userIdx);

            // 해당 단계의 성장치 불러오기
            int plantMaxScore = Constant.PLANT_LEVEL[plantLevel]; // 성장치

            // 성장치 MAX인가? == 최대 단계 & 최대 점수인 경우 -> 성장치 감소 X (바로 response 반환)
            int maxLevel = plantDao.getMaxLevel(userIdx);
            if (plantLevel == maxLevel && plantScore == plantMaxScore) {
                return result;
            }

            // 단계가 하나씩 줄어드는게 아니라 n단계씩 줄 수 있기 때문에 생각해야 할 것이 많음
            // public static final int[] PLANT_LEVEL = new int[] {15, 30, 50, 50};

            // 누적 점수 가져오기
            int plantScore_sum = 0;
            if (plantLevel != 0) {
                for (int i = 0; i < plantLevel; i++) { // 전 단계까지 모두 누적
                    plantScore_sum += Constant.PLANT_LEVEL[i];
                }
            }
            plantScore_sum += plantScore; // 누적 점수

            // 누적 점수 += score(음수)
            plantScore_sum += score; // 변경된 점수

            int plantLevel_current = 0; // 현재 식물 단계
            if (plantScore_sum < 0) {
                plantScore = 0; // 단계 : 0, 점수 : 0 (초기화)

            } else {
                // 현재 식물 단계 구하기
                int score_sum = 0; // 누적 성장치
                plantLevel_current = -1; // 현재 식물 단계
                for (int i = 0; i <= maxLevel; i++) {
                    score_sum += Constant.PLANT_LEVEL[++plantLevel_current];
                    if (plantScore_sum <= score_sum) { // 해당 단계에서 for문 멈추기
                        break;
                    }
                }

                // 현재 식물 점수 구하기
                if (plantLevel_current != 0) {
                    score_sum = 0; // 현재 단계까지 누적 성장치
                    for (int i = 0; i < plantLevel_current; i++) {
                        score_sum += Constant.PLANT_LEVEL[i];
                    }
                    plantScore = plantScore_sum - score_sum; // 점수 변경 (누적 제거)
                }
            }

            if (plantLevel_current != plantLevel) {
                plantDao.setLevel(userIdx, plantLevel_current); // 변경된 단계 반영
                result.setLevelChanged(true); // 단계가 변경되었다는 정보를 response에 넣기
            }

            plantDao.setScore(userIdx, plantScore); // 변경된 점수 반영
            result.setStatus("levelDown");
            return result;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //화분 점수 증가 (Service)
    /*
    public PatchUpDownScoreRes upScore(int userIdx, PatchUpDownScoreReq patchUpDownScoreReq) throws BaseException {
        int addScore = patchUpDownScoreReq.getAddScore();
        try {
            //화분이 시무룩 상태면 성장치 오르지 않음
            if(plantProvider.checkSad(userIdx) == true)
                throw new BaseException(SAD_STATUS_PLANT); //화분이 시무룩 상태입니다. 화분의 성장치를 증가시킬 수 없습니다.

            //점수 변경
            if (plantDao.plusScore(userIdx, addScore) == 1) { //점수 변경 성공
                //TODO: 1. 점수 증가 후의 score 가져오기
                //      2. 가져온 점수가 단계 증가 조건에 충족되면 "화분 단계 변경 (Service)" 호출

                //점수에 따라서 patchUpDownScoreReq 필드 1로 Set
                PatchUpDownScoreRes patchUpDownScoreRes = new PatchUpDownScoreRes();
                if (addScore == 5) //PLANT_LEVELUP_DIARY
                    patchUpDownScoreRes.setDiary_upScore(1);
                else if (addScore == 3) //PLANT_LEVELUP_LETTER
                    patchUpDownScoreRes.setLetter_upScore(1);

                //1번
                int currentScore = plantProvider.selectScore(userIdx); //증가 후의 score 가져오기
                int currentLevel = plantProvider.selectLevel(userIdx); //level 가져오기(점수가 증가는 됐지만 그에 따른 영향은 아직 안받은 상태)

                // 2번
                Boolean condFor0 = (currentScore == PLANT_LEVEL_0 && currentLevel == 0); //현재 score = 15 && 현재 레벨 0
                Boolean condFor1 = (currentScore == PLANT_LEVEL_1 && currentLevel == 1); //현재 score = 30 && 현재 레벨 1
                Boolean condFor2 = (currentScore == PLANT_LEVEL_2 && currentLevel == 2); //현재 score = 30 && 현재 레벨 2
                Boolean condFor3 = (currentScore == PLANT_LEVEL_3 && currentLevel == 3); //현재 score = 50 && 현재 레벨 3
                Boolean condFor4 = (currentScore == PLANT_LEVEL_4 && currentLevel == 4); //현재 score = 70 && 현재 레벨 4
                Boolean condFor5 = (currentScore == PLANT_LEVEL_5 && currentLevel == 5); //현재 score = 70 && 현재 레벨 5
                if (condFor0 || condFor1 || condFor2 || condFor3 || condFor4 || condFor5) { //점수 충족 -> 화분 단계 변경 (Service) 호출

                    if (upLevel(userIdx) == 1) { //점수 up && 레벨 up --> return 1
                        patchUpDownScoreRes.setUpLevel(1);
                        return patchUpDownScoreRes; //변경 성공
                    } else {
                        throw new BaseException(MODIFY_FAIL_LEVEL); //변경 실패
                    }

                } else { //점수만 up (점수 충족 X)
                    return patchUpDownScoreRes;
                }

            } else { //점수 변경 실패
                throw new BaseException(MODIFY_FAIL_SCORE);
            }
        } catch (Exception exception) {
            throw exception;
        }
    }
     */

    //화분 단계 증가 (Service)
    /*
    public int upLevel(int userIdx) throws BaseException {
        try {
            return plantDao.upLevel(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
    */

    //화분 점수 감소 API
    /*
    public PatchUpDownScoreRes downScore(int userIdx, PatchUpDownScoreReq patchUpDownScoreReq) throws BaseException {
        try {
            // TODO 1. 프리미엄 계정인지 확인
            //      2. 프리미엄 계정이면 성장치 감소 X -> 알려주기
            //      3. 식물의 성장치가 MAX인지 확인
            //      4. MAX이면 성장치 감소 X -> 알려주기
            //      5. 프리미엄 계정이 아니면 or MAX가 아니면 점수가 0점인지 확인
            //      6. 현재 점수 0 & 단계 0 이면 적절한 Status 리턴
            //      7. 0이 아니면 변경 하러 go

            // 1, 2번
            if (plantDao.checkPremium(userIdx).equals("premium")) //프리미엄 계정이면
                throw new BaseException(PREMIUM_USER); //프리미엄 계정 회원은 화분 성장치가 감소하지 않습니다.

            // 3, 4번
            // TODO 1. UserPlantList 테이블에서 selected 화분의 level을 가져온다
            //      2. Plant테이블에서 가져온 화분의 maxlevel을 가져온다
            //      3. 비교해서 같으면 MAX
            int currentLevel = plantProvider.selectLevel(userIdx); //현재 레벨
            int maxLevel = plantProvider.maxLevel(userIdx); //최대 레벨
            if (currentLevel == maxLevel)
                throw new BaseException(MAXLEVEL_PLANT); //성장치가 MAX 단계에 도달한 화분은 성장치가 감소하지 않습니다.

            // 5 ~ 7번
            int currentScore = plantProvider.selectScore(userIdx); //현재 점수
            if (currentLevel != 0 && currentScore != 0) {
                int addScore = patchUpDownScoreReq.getAddScore();
                PatchUpDownScoreRes patchUpDownScoreRes = new PatchUpDownScoreRes();

                //점수에 따라서 patchUpDownScoreReq 필드 1로 Set
                if (addScore == -10) //PLANT_LEVELDOWN_DIARY
                    patchUpDownScoreRes.setDiary_downScore(1);
                else if (addScore == -30) //PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE
                    patchUpDownScoreRes.setReport_spam_dislike_downScore(1);
                else if (addScore == -100) //PLANT_LEVELDOWN_REPORT_SEX_HATE
                    patchUpDownScoreRes.setReport_sex_hate_downScore(1);

                //점수 감소, 감소 실패시 호출한 함수에서 예외처리 됨
                downScoreAndLevel(currentScore, addScore, currentLevel, userIdx);
                return patchUpDownScoreRes;

            } else { //6번
                throw new BaseException(INVALID_SCORE_PLANT);
            }
        } catch (Exception exception) {
            throw exception;
        }
    }
    */

    //화분 점수 감소를 실제로 수행하는 함수
    /*
    public int downScoreAndLevel(int curScore, int addScore, int currentLevel, int userIdx) throws BaseException {
        int addRes = curScore + addScore; //현재 점수 + 감소시킬 점수(음수)

        if (addRes < 0) { //음수 이므로 단계도 감소

            try {
                if (currentLevel == 0) //단계를 감소시켜야 하는데 0단계라서 감소시킬 수 없음
                    throw new BaseException(INVALID_LEVEL_PLANT); //동작을 수행할 화분의 단계가 0단계이므로 더 이상 단계와 점수를 감소시킬 수 없습니다.
            } catch (Exception e) {
                throw e;
            }

            int totalDec = 0; //단계 감소 횟수
            while (addRes >= 0) { //addRes가 양수 or 0이 되면 탈출

                ++totalDec;
                int prevLevel = currentLevel - totalDec; //현재 레벨(감소 전) - totalDec

                int prevMaxScore = 0; //감소 전 단계의 최대 점수;
                if (prevLevel == 0)
                    prevMaxScore = PLANT_LEVEL_0;
                else if (prevLevel == 1)
                    prevMaxScore = PLANT_LEVEL_1;
                else if (prevLevel == 2)
                    prevMaxScore = PLANT_LEVEL_2;
                else if (prevLevel == 3)
                    prevMaxScore = PLANT_LEVEL_3;
                else if (prevLevel == 4)
                    prevMaxScore = PLANT_LEVEL_4;

                addRes += prevMaxScore;

                try {
                    if (prevLevel <= 0 && addRes < 0) //계속 감소시키다가 0레벨 (음수)점됨. 더 감소 불가능.
                        throw new BaseException(INVALID_LEVEL_PLANT); //동작을 수행할 화분의 단계가 0단계이므로 더 이상 단계와 점수를 감소시킬 수 없습니다.
                } catch (Exception e) {
                    throw e;
                }
            }
            //양수 or 0점이 되서 탈출
            //점수 감소시키는 Dao 함수 호출
            //그다음 단계 감소시키는 Dao 함수 호출

            try {
                if (plantDao.setDownScore(userIdx, addRes) != 1) //점수 감소 실패
                    throw new BaseException(MODIFY_FAIL_SCORE); //화분 점수 변경에 실패하였습니다.
                else { //점수 감소 성공
                    if (plantDao.downLevel(userIdx, totalDec) != 1) //레벨 감소 실패
                        throw new BaseException(MODIFY_FAIL_LEVEL); //화분 단계 변경에 실패하였습니다.
                    else //레벨 감소 성공
                        return 1;
                }
            } catch (Exception e) {
                throw e;
            }
        } else { //점수만 감소
            return plantDao.plusScore(userIdx, addScore);
        }
    }
     */

}

