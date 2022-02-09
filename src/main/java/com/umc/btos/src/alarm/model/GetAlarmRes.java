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
}
