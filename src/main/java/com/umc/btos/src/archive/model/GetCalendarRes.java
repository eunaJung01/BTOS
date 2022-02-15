package com.umc.btos.src.archive.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCalendarRes {
    private int diaryIdx;
    private String diaryDate; // yyyy.MM.dd
    private int doneListNum; // 하루에 저장되어 있는 done list 개수
    private int emotionIdx; // 감정 이모티콘 식별자 (1~8)

    public GetCalendarRes(int diaryIdx, String diaryDate) {
        this.diaryIdx = diaryIdx;
        this.diaryDate = diaryDate;
    }

}
