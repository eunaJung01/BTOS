package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//상점에서 목록을 띄우는 용도의 클래스
public class GetPlantRes {
    private int plantIdx;
    private String plantName;
    private String plantImgUrl; //text ~ MySQL (default = null)
    private int plantPrice; //unsigiend int ~ MySQL
    private int maxLevel;
    private String userStatus; //유저의 화분 보유 상태 : active(보유중) / delete(아님) (status FROM UserPlantList 테이블)
    private int selectedPlantIdx; //보유중인 화분중 선택된 화분 (plantIdx FROM UserPlantList 테이블)
}
