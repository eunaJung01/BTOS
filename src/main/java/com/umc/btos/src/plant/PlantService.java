package com.umc.btos.src.plant;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponseStatus;
import com.umc.btos.src.plant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

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
    public int activePlant(int userIdx) throws BaseException {
        try {
            if (plantDao.activePlant(userIdx) == 1)
                return 3; //성공 : 3 반환
            return -1;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //화분 선택 API
    public BaseResponseStatus selectPlant(PatchSelectPlantReq patchSelectPlantReq) throws BaseException {
        try {
            //회원이 futurePlant를 이미 selected된 화분으로 넘겼는지 체크
            int checkRes = plantProvider.checkPlant(patchSelectPlantReq);
            if (checkRes != 3) //넘겼다면
                return INVALID_IDX_PLANT;

            //기존에 선택되어있던 화분의 status를 active로 바꾸자 (selected -> active)
            int selectRes = activePlant(patchSelectPlantReq.getUserIdx());
            if (selectRes != 3) //상태 변경 실패
                return MODIFY_FAIL_STATUS;

            //status: active -> selected
            if (plantDao.selectPlant(patchSelectPlantReq) == 1) //변경 성공시
                return SUCCESS;
            else //변경 실패시
                return MODIFY_FAIL_STATUS;
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
                return MODIFY_FAIL_BUY_PLANT;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //화분 점수 증가 (Service)
    public PatchUpDownScoreRes upScore(int userIdx, PatchUpDownScoreReq patchUpDownScoreReq) throws BaseException {
        //userIdx : Dao에서 조건(WHERE) 걸 때 필요 - 해당 회원의 화분 점수 증가
        try {
            if (plantDao.upScore(userIdx, patchUpDownScoreReq.getAddScore()) == 1) { //점수 변경 성공
                //TODO: 1. 점수 증가 후의 score 가져오기
                //      2. 가져온 점수가 단계 증가 조건에 충족되면 "화분 단계 변경 (Service)" 호출

                //1번
                int currentScore = plantProvider.selectScore(userIdx); //score 가져오기
                int currentLevel = patchUpDownScoreReq.getCurrentLevel(); //입력받은 현재 레벨

                // 2번
                Boolean condFor0 = (currentScore == 15 && currentLevel == 0); //현재 score = 15 && 현재 레벨 0
                Boolean condFor1 = (currentScore == 30 && currentLevel == 1); //현재 score = 30 && 현재 레벨 1
                Boolean condFor2 = (currentScore == 30 && currentLevel == 1); //현재 score = 30 && 현재 레벨 2
                Boolean condFor3 = (currentScore == 50 && currentLevel == 1); //현재 score = 50 && 현재 레벨 3
                Boolean condFor4 = (currentScore == 70 && currentLevel == 1); //현재 score = 70 && 현재 레벨 4
                Boolean condFor5 = (currentScore == 70 && currentLevel == 1); //현재 score = 70 && 현재 레벨 5
                if (condFor0 || condFor1 || condFor2 || condFor3 || condFor4 || condFor5) { //점수 충족 -> 화분 단계 변경 (Service) 호출
                    if (modifyLevel(userIdx) == 1) { //점수 up && 레벨 up --> return 2
                        PatchUpDownScoreRes patchUpDownScoreRes = new PatchUpDownScoreRes(2);
                        return patchUpDownScoreRes; //변경 성공
                    } else
                        throw new BaseException(MODIFY_FAIL_LEVEL); //변경 실패
                } else { //점수 up (점수 충족 X) --> return 1
                    PatchUpDownScoreRes patchUpDownScoreRes = new PatchUpDownScoreRes(1);
                    return patchUpDownScoreRes;
                }
            } else //점수 변경 실패
                throw new BaseException(MODIFY_FAIL_SCORE);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //화분 단게 변경 (Service)
    public int modifyLevel(int userIdx) throws BaseException {
        try {
            return plantDao.modifyLevel(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
