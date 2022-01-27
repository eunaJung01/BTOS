package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//Profile에서 회원이 선택한 화분을 조회했을 때를 위한 클래스
public class GetSpecificPlantRes {
    private int plantIdx; //화분 식별자
    private String plantName; //화분 이름
    private String plantImgUrl; //화분 이미지
    private String plantInfo; // 화분 정보
    private int plantPrice; //화분 가격 (미보유)
    private int maxLevel; //화분의 최대 단계
    private int currentLevel; //현재 화분 단계 (보유), 미보유시 -1로 set
    private int plantStatus; //회원의 화분 보유 여부 : 보유 1, 미보유 0
}
