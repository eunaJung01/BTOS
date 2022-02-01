package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//화분 선택 시 Body
public class PatchSelectPlantReq {
    private int userIdx;
    private int plantIdx; // 사용자가 바꾸기로 선택한 화분(status: active -> selected)
}
