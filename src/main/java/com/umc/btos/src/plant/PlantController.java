package com.umc.btos.src.plant;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.config.Constant;
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
     * 화분목록조회(Profile + 상점) API
     * [GET] /plants/:userIdx
     * Path variable : userIdx (mandatory: Y)
     *
     * userIdx 기준으로 보유(active/selected)/미보유한 모든 화분에 대해 조회
     */
    @ResponseBody
    @GetMapping("{userIdx}")
    public BaseResponse<List<GetPlantRes>> getPlantList(@PathVariable("userIdx") int userIdx) {
        try {
            //조회 성공 시 : List<GetPlantRes> 형태로 결과(화분목록) 반환 - 1000
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            List<GetPlantRes> getPlantRes = plantProvider.getPlantList(userIdx); //조회(read) -> Provider
            return new BaseResponse<>(getPlantRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 회원이 선택한 화분 조회 API
     * [GET] /plants?plantIdx=&userIdx=
     * Query String : plantIdx, userIdx (mandatory: Y)
     *
     * userIdx가 선택한 plantIdx 조회
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<GetPlantRes> getSelectedPlant(@RequestParam("plantIdx") int plantIdx,
                                                      @RequestParam("userIdx") int userIdx) {
        try {
            //조회 성공 시 : GetSpecificPlantRes 형태로 결과 반환 - 1000
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantProvider.getSelectedPlant(plantIdx, userIdx)); //조회(read) -> Provider
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 화분 선택 API
     * [PATCH] /plants/select
     * RequestBody : PatchSelectPlantReq - 필드명 userIdx, plantIdx (mandatory: Y)
     *
     * userIdx가 BODY로 넘긴 plantIdx의 status를 selected로 변경
     */
    @ResponseBody
    @PatchMapping("select")
    public BaseResponse<String> selectPlant(@RequestBody PatchSelectPlantReq patchSelectPlantReq) {
        try {
            //status 변경 성공시 : "요청에 성공하였습니다." - 1000
            //selected -> active 실패시 : "화분 상태 변경에 실패하였습니다." - 7010
            //futurePlant값이 이미 선택된 화분인 경우 : "이미 선택된 화분입니다." - 5005
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantService.selectPlant(patchSelectPlantReq)); //선택(update) -> Service
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 화분 구매(보유) API
     * [POST] /plants/buy
     * RequestBody : PostBuyPlantReq - 필드명 userIdx, plantIdx (mandatory: Y)
     *
     * userIdx가 Body로 넘긴 plantIdx 구매
     */
    @ResponseBody
    @PostMapping("buy")
    public BaseResponse<String> buyPlant(@RequestBody PostBuyPlantReq postBuyPlantReq) {
        try {
            //행 추가 성공시 : "요청에 성공하였습니다." - 1000
            //       실패시 : "화분 상태 변경에 실패하였습니다." - 7011
            //중복 구매 시도시 : "이미 보유한 화분입니다." - 7016
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantService.buyPlant(postBuyPlantReq)); //구매(post) -> Service
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 화분 개수 조회 API
     * [GET] /plants/count
     *
     * Plant 테이블에 있는 모든 화분의 개수 조회
     */
    @ResponseBody
    @GetMapping("count")
    public BaseResponse<GetCountPlantRes> countPlant() {
        try {
            // 성공 시 : "요청에 성공하였습니다." - 1000
            // DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            GetCountPlantRes getCountPlantRes = new GetCountPlantRes(plantProvider.countPlant()); //조회(read) -> Provider
            return new BaseResponse<>(getCountPlantRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    // =================================== 화분 점수 및 단계 변경 테스트 ===================================

    @ResponseBody
    @GetMapping("/test/plus")
    public BaseResponse<PatchModifyScoreRes> modifyScore_plus(@RequestParam("userIdx") int userIdx) {
        try {
            PatchModifyScoreRes result = plantService.modifyScore_plus(userIdx, Constant.PLANT_LEVELUP_DIARY, "diary");
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/test/minus")
    public BaseResponse<PatchModifyScoreRes> modifyScore_minus(@RequestParam("userIdx") int userIdx) {
        try {
            PatchModifyScoreRes result = plantService.modifyScore_minus(userIdx, Constant.PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE, "report_spam_dislike");
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}