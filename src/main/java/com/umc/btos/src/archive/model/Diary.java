package com.umc.btos.src.archive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class Diary {
    private int diaryIdx;
    private int doneListNum;
    private int emotionIdx;
    private String diaryDate; // yyyy.MM.dd
    private String content;

    public Diary(int diaryIdx, int emotionIdx, String diaryDate, String content) {
        this.diaryIdx = diaryIdx;
        this.emotionIdx = emotionIdx;
        this.diaryDate = diaryDate;
        this.content = content;
    }

}
