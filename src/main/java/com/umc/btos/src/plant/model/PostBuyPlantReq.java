package com.umc.btos.src.plant.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class PostBuyPlantReq {
    private int userIdx; // 사용자 식별자
    private int plantIdx; // 구매할 화분의 식별자
}
