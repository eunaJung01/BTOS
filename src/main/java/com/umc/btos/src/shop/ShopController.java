package com.umc.btos.src.shop;

import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/btos/shops")
public class ShopController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    @Autowired
    private final ShopProvider shopProvider;
         */
    @Autowired
    private final ShopService shopService;

    public ShopController(ShopService shopService){ //ShopProvider shopProvider){
        //this.shopProvider = shopProvider;
        this.shopService = shopService;
    }

    /**
     * 프리미엄 계정으로 변경 API
     * [PATCH] /btos/shops/:userIdx/join
     * Path Variable : userIdx (mandatody: Y)
     */
    @ResponseBody
    @PatchMapping("{userIdx}/join")
    public BaseResponse<String> joinPremium(@PathVariable("userIdx") int userIdx) {
        try{
            // 변경 성공 시 : "요청에 성공하였습니다." - 1000
            // 변경 실패 시 : "프리미엄 계정 변경에 실패하였습니다." - 7020
            // DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(shopService.joinPremium(userIdx));
        }catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 청약철회 (프리미엄 취소) API
     * [PATCH] /btos/shops/:userIdx/withdraw
     * Path Variable : userIdx (mandatory: Y)
     */

}
