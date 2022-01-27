package com.umc.btos.src.shop;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class ShopService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ShopDao shopDao;

    @Autowired
    public ShopService(ShopDao shopDao) {
        this.shopDao = shopDao;
    }

    //프리미엄 계정으로 변경 API
    public BaseResponseStatus joinPremium(int userIdx) throws BaseException {
        try {
            if (shopDao.joinPremium(userIdx) == 1) //변경 성공
                return SUCCESS;
            else //변경 실패
                return MODIFY_FAIL_PREMIUM;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    //청약철회 API
    public BaseResponseStatus withdrawPremium(int userIdx) throws BaseException {
        try {
            if (shopDao.withdrawPremium(userIdx) == 1) //변경 성공
                return SUCCESS;
            else //변경 실패
                return MODIFY_FAIL_WITHDRAW;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
