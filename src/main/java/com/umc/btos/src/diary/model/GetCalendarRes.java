package com.umc.btos.src.diary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetCalendarRes {
    private String diaryDate; // YYYY-MM-DD
    private int doneListNum; // 하루에 저장되어 있는 done list 개수
    private int emotionIdx;
}
