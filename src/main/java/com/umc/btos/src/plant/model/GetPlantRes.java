package com.umc.btos.src.plant.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GetPlantRes {
    private int plantIdx; // 화분 식별자
    private String plantName; // 화분 이름
    private String plantInfo; // 화분 정보
    private int plantPrice; // 화분 가격
    private int maxLevel; //최대 레벨
    private int currentLevel; //현재 레벨 : 보유중 - 현재 레벨 or 미보유 -1
    private String plantStatus; //유저의 화분 상태 : active/selected/inactive
    private Boolean isOwn; // 유저의 화분 보유 여부 : 1/0

    public GetPlantRes() {
        this.currentLevel = -1;
        this.isOwn = true;
    }
}
