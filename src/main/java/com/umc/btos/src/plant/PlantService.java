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

    @Autowired
    public PlantService(PlantDao plantDao){
        this.plantDao = plantDao;
    }

    //화분 선택 API
    public BaseResponseStatus selectPlant(int uPlantIdx) throws BaseException {
        try{
            if(plantDao.selectPlant(uPlantIdx) == 1) //변경 성공시
                return SUCCESS;
            else //변경 실패시
                throw new BaseException(MODIFY_FAIL_STATUS);
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //화분 구매(보유) API
    public BaseResponseStatus buyPlant(int plantIdx, int userIdx) throws BaseException {
        try{
            if(plantDao.buyPlant(plantIdx, userIdx) == 1) //변경 성공시
                return SUCCESS;
            else //구매 실패시
                throw new BaseException(MODIFY_FAIL_BUY_PLANT);
        }catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
