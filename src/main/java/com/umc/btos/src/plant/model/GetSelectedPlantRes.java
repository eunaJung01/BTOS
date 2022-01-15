package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//Profile에서 회원이 선택한 화분을 조회했을 때를 위한 클래스
public class GetSelectedPlantRes {
    private int plantIdx; //화분 식별자
    private String plantName; //화분 이름
    private int maxLevel; //보유 중인 화분의 최대 단계 (Plant.maxLevel)
    private String plantImgUrl; //화분 이미지
    private int currentLevel; //현재 화분 단계 (UserPlanList.level)
    private String userStatus; //유저의 화분 보유 상태
    private int selectedPlantIdx; //보유중인 화분중 선택된 화분
}
