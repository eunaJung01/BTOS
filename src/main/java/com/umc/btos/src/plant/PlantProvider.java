package com.umc.btos.src.plant;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.plant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

//Read
@Service
public class PlantProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PlantDao plantDao;

    @Autowired
    public PlantProvider(PlantDao plantDao) {
        this.plantDao = plantDao;
    }


    //회원이 선택한 화분 조회 API
    public GetPlantRes getSelectedPlant(int plantIdx, int userIdx) throws BaseException {
        try {
            int status = plantDao.checkPlantExist(plantIdx, userIdx); //plantIdx인 화분을 사용자가 보유중이면 1, 미보유 0
            return plantDao.getSelectedPlant(plantIdx, status, userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //화분 선택 API ~ 회원이 plantIdxt를 이미 selected된 화분으로 넘겼는지 체크하기 위한 함수
    public int checkPlant(PatchSelectPlantReq patchSelectPlantReq) throws BaseException {
        try { //회원이 plantIdx를 이미 selected된 화분으로 넘겼으면
            if (patchSelectPlantReq.getPlantIdx() == plantDao.checkPlant(patchSelectPlantReq.getUserIdx()))
                return -1;
            else //아니면 1 반환
                return 1;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //모든 화분 조회(Profile + 상점)
    public List<GetPlantRes> getPlantList(int userIdx) throws BaseException {
        try {
            List<Integer> plantIdxList = plantDao.getPlantIdx(); //Plant 테이블에 있는 모든 plantIdx
            List<Integer> userPlantIdxList = plantDao.getUserPlantIdx(userIdx); //해당 회원이 가지고 있는 모든 plantIdx

            return plantDao.getPlantList(userIdx, plantIdxList, userPlantIdxList);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //화분 개수 조회 API
    public int countPlant() throws BaseException {
        try{
            return plantDao.countPlant();
        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /*
    //화분의 현재 score 가져오기
    public int selectScore(int userIdx) throws BaseException {
        try {
            return plantDao.selectScore(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
    //화분의 현재 Level 가져오기
    public int selectLevel(int userIdx) throws BaseException {
        try {
            return plantDao.selectLevel(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
    //userIdx의 selected 화분의 MAX Level 가져오기
    public int maxLevel(int userIdx) throws BaseException {
        try{
            return plantDao.maxLevel(userIdx);
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
    //회원이 프리미엄 계정인지 확인
    public String checkPremium(int userIdx) throws BaseException {
        try{
            return plantDao.checkPremium(userIdx);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
    //화분이 시무룩 상태인지 확인
    public boolean checkSad(int userIdx) throws BaseException {
        try{
            return plantDao.checkSad(userIdx);
        } catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
    */
}

