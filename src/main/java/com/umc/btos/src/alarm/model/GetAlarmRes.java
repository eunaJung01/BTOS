package com.umc.btos.src.alarm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetAlarmRes {
    private int userIdx;
    private String alarmType;
    private int reqParamIdx;
    /*
     * alarmType = diary / letter / reply -> reqParamIdx = 0 (우편함 목록 조회)
     * alarmType = plant -> reqParamIdx = uPlantIdx (해당 화분 조회)
     * alarmType = report_diary -> reqParamIdx = diaryIdx (신고 당한 일기 조회)
     * alarmType = report_letter -> reqParamIdx = letterIdx (신고 당한 편지 조회)
     * alarmType = report_reply -> reqParamIdx = replyIdx (신고 당한 답장 조회)
     * alarmType = notice -> reqParamIdx = noticeIdx (해당 공지사항 조회)
     */
}
