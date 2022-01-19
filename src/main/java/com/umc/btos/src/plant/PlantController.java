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
@RequestMapping("/btos/plant")
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
     * [GET] /btos/plant/list?userIdx
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
     * [GET] /btos/plant/uSelected?plantIdx=&userIdx=
     * Query String : plantIdx, userIdx (mandatory: Y)
     */
    //'회원'이 보유중인 것 중에 선택하는 것이므로 userIdx도 필요함
    //회원이 일단 보유중이어야 함 (Profile 쪽에 있는 걸 보아하니) -> UserPlantList.status = 'active'
    // active상태의 화분 && 선택 행위를 한 userIdx && 화분 보유 및 선택 여부
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


    //아래의 3 API 모두 입력한 userIdx를 WHERE(조건)걸어서 바꿔야함

    /**
     * 화분 선택 API
     * [PATCH] /btos/plant/:uPlantIdx
     * Path Variable : plantIdx (mandatory: Y)
     */
    @ResponseBody
    @PatchMapping("{uPlantIdx}")
    public BaseResponse<String> selectPlant(@PathVariable int uPlantIdx) { //userIdx가 굳이 필요한가?
        try {
            //status 변경 성공시 : "요청에 성공하였습니다." - 1000
            //           실패시 : "화분 선택에 실패하였습니다." - 7000
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantService.selectPlant(uPlantIdx));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 화분 구매(보유) API
     * [POST] /btos/plant/buy?plantIdx=&userIdx=
     * Query String : plantIdx, userIdx (mandatory: Y)
     */
    @ResponseBody
    @PostMapping("buy")
    public BaseResponse<String> buyPlant(@RequestParam("plantIdx") int plantIdx,
                                         @RequestParam("userIdx") int userIdx) {
        try {
            //행 추가 성공시 : "요청에 성공하였습니다." - 1000
            //       실패시 : "화분 구매에 실패하였습니다." - 7010
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            return new BaseResponse<>(plantService.buyPlant(plantIdx, userIdx));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

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