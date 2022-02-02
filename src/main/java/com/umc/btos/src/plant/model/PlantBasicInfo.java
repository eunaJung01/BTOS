package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//임시로 저장하는 공간
//Plant 테이블 정보 저장
public class PlantBasicInfo {
    private int plantIdx; //화분 식별자
    private String plantName; //화분 이름
    private String plantInfo; // 화분 정보
    private int plantPrice; //화분 가격 (미보유)
    private int maxLevel; //화분의 최대 단계
}