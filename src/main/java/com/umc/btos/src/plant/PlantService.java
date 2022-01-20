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
            if(checkRes != 3) //넘겼다면
                return INVALIDE_IDX_PLANT;

            //기존에 선택되어있던 화분의 status를 active로 바꾸자 (selected -> active)
            int selectRes = activePlant(patchSelectPlantReq.getUserIdx());
            if(selectRes != 3) //상태 변경 실패
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

}
