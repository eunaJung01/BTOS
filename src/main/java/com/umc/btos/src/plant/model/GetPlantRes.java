package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//Profile에서 목록을 띄우는 용도의 클래스
public class GetPlantRes {
    private int plantIdx; // 화분 식별자
    private String plantName; // 화분 이름
    private String plantImgUrl; //text ~ MySQL (default = null)
    private int plantPrice; //unsigiend int ~ MySQL
    private int maxLevel; //최대 레벨
    private int currentLevel; //현재 레벨 (UserPlanList.level) , maxLevel이랑 동일하면 (MAX) 표기
    private String userStatus; //유저의 화분 보유 상태 : active(보유중) / delete(아님) (status FROM UserPlantList 테이블)
}
