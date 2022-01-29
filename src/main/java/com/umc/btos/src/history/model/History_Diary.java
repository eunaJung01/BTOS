package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class History_Diary {
    private String diaryContent;
    private int emotionIdx;
    private int doneListNum;
}
