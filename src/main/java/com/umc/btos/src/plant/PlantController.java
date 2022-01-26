package com.umc.btos.src.plant;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.plant.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/plants")
public class PlantController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PlantProvider plantProvider;
    @Autowired
    private final PlantService plantService;

    public PlantController(PlantProvider plantProvider, PlantService plantService) {
        this.plantProvider = plantProvider;
        this.plantService = plantService;
    }


    /**
     * 화분목록조회(Profile) API
     * [GET] /btos/plants/:userIdx/list
     * Path variable : userIdx (mandatory: Y)
     */
    @ResponseBody
    @GetMapping("{userIdx}/list")
    public BaseResponse<List<GetPlantRes>> getPlantList(@PathVariable("userIdx") int userIdx) {
        try {
            //조회 성공 시 : List<GetPlantRes> 형태로 결과(화분목록) 반환 - 1000
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            List<GetPlantRes> getPlantRes = plantProvider.getAllPlant(userIdx); //조회(read) -> Provider
            return new BaseResponse<>(getPlantRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 회원이 선택한 화분 조회 API
     * [GET] /btos/plants?plantIdx=&userIdx=
     * Query String : plantIdx, userIdx (mandatory: Y)
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<GetSpecificPlantRes> getSelectedPlant(@RequestParam("plantIdx") int plantIdx,
                                                              @RequestParam("userIdx") int userIdx) {
        try {
            //조회 성공 시 : GetSpecificPlantRes 형태로 결과 반환 - 1000
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantProvider.getSelectedPlant(plantIdx, userIdx));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 화분 선택 API
     * [PATCH] /btos/plants/select
     * RequestBody : PatchSelectPlantReq - 필드명 userIdx, futurePlant(=uPlantIdx) (mandatory: Y)
     */
    @ResponseBody
    @PatchMapping("select")
    public BaseResponse<String> selectPlant(@RequestBody PatchSelectPlantReq patchSelectPlantReq) {
        try {
            //status 변경 성공시 : "요청에 성공하였습니다." - 1000
            //selected -> active 실패시 : "화분 상태 변경에 실패하였습니다." - 7010
            //futurePlant값이 이미 선택된 화분인 경우 : "이미 선택된 화분입니다." - 7015
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantService.selectPlant(patchSelectPlantReq));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 화분 구매(보유) API
     * [POST] /btos/plants/buy
     * RequestBody : PostBuyPlantReq - 필드명 userIdx, plantIdx (mandatory: Y)
     */
    @ResponseBody
    @PostMapping("buy")
    public BaseResponse<String> buyPlant(@RequestBody PostBuyPlantReq postBuyPlantReq) {
        try {
            //행 추가 성공시 : "요청에 성공하였습니다." - 1000
            //       실패시 : "화분 상태 변경에 실패하였습니다." - 7010
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantService.buyPlant(postBuyPlantReq));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}