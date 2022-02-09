package com.umc.btos.src.alarm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetAlarmRes {
    private String alarmType;
    private int typeIdx;
    // Alarm.type == diary / letter / reply -> userIdx (우편함 목록 조회)
    // Alarm.type == plant -> uPlantIdx (해당 화분 조회)
    // Alarm.type == report -> 뭐였냐에 따라 diaryIdx, letterIdx, replyIdx
}
