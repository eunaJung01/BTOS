package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

/*
점수 증가 또는 레벨 증가 성공 시

<반환값>
점수 증가 시 : 1
레벨 증가 시 : 2
 */

public class PatchUpScoreRes {
    private int upScoreOrLevel;
}
