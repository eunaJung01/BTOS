package com.umc.btos.src.diary.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCalendarRes {
    private String diaryDate; // YYYY-MM-DD
    private int doneListNum; // 하루에 저장되어 있는 done list 개수
    private int emotionIdx;

    public GetCalendarRes(String diaryDate) {
        this.diaryDate = diaryDate;
    }

}
