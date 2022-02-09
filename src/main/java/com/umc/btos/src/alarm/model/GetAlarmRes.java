package com.umc.btos.src.alarm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetAlarmRes {
    private int alarmIdx;
    private String content;
    private String createdAt; // 알림 수신 시각 (yyyy-MM-dd HH:mm:ss)
}
