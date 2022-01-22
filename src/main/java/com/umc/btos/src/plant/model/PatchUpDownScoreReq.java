package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class PatchUpDownScoreReq {
    /* addScore : 더해야 할 점수
    ex1. 일기 하나 작성 : score +5 -> 5점이 addScore
    ex2. 5일 이상 접속안한 경우 score -5 -> -5점이 addScore
     */
    private int addScore;
    private int currentLevel; //화분의 현재 레벨(점수 증가 전)
                              //주의 사항 : 점수 감소인 경우 반드시 음수 입력
}
