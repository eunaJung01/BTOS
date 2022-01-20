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
@RequestMapping("/btos/plants")w
public class PlantController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PlantProvider plantProvider;
    @Autowired
    private final PlantService plantService;
    /*
    @Autowired
    다른 클래스들 provider, service 등
     */

    public PlantController(PlantProvider plantProvider, PlantService plantService) {
        this.plantProvider = plantProvider;
        this.plantService = plantService;
    }


    /**
     * 화분목록조회(Profile) API
     * [GET] /btos/plants/list?userIdx
     * Query String : userIdx (mandatory: Y)
     */
    @ResponseBody
    @GetMapping("list")
    public BaseResponse<List<GetPlantRes>> getPlantList(@RequestParam("userIdx") int userIdx) {
        try {
            //조회 성공 시 : List<GetPlantRes> 형태로 결과(화분목록) 반환
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            List<GetPlantRes> getPlantRes = plantProvider.getAllPlant(userIdx); //조회(read) -> Provider
            return new BaseResponse<>(getPlantRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 회원이 선택한 화분 조회 API
     * 보류 : 더보기 기능 추가시 다시 진행
     * "미보유 화분"도 더보기 기능이 가능하다면 userIdx안 받고 plantIdx만 받으면 됨
     *      1. Controller, Provider, Dao 인자 수정
     *      2. Dao 쿼리 문 수정 -> WHERE Plant.status="active" AND Plant.plnatIdx=입력한 화분 식별자(plantIdx)
     *      3. 만약 조회한 화분이 회원이 보유중이라면..? 이경우까지.. 음 그러니까 이렇게까지 정보를 출력해야한다면 userIdx 받아야함
     * [GET] /btos/plants/uSelected?plantIdx=&userIdx=
     * Query String : plantIdx, userIdx (mandatory: Y)
     */
    @ResponseBody
    @GetMapping("uSelected")
    public BaseResponse<GetSpecificPlantRes> getSelectedPlant(@RequestParam("plantIdx") int plantIdx,
                                                              @RequestParam("userIdx") int userIdx) {
        try {
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

    /**
     * 화분 점수 반영 및 단계 변경 API
     * [PATCH] /btos/plants/level
     */


    /*
     * 화분 보유중 목록 조회 API
     * [GET] /btos/plant/own?userIdx=
     * Query String : userIdx (mandatory: Y)

    @ResponseBody
    @GetMapping("own")
    public BaseResponse<List<GetSpecificPlantRes>> getOwnPlantList(@RequestParam("userIdx") int userIdx) {
        try {
            return new BaseResponse<>(plantProvider.getOwnPlantList(userIdx));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
    */
}