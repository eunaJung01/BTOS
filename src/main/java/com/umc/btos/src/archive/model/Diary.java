package com.umc.btos.src.archive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Diary {
    private int diaryIdx;
    private int emotionIdx;
    private String diaryDate; // yyyy.MM.dd
    private String content;
    private List<Done> doneList;

    public Diary(int diaryIdx, int emotionIdx, String diaryDate, String content) {
        this.diaryIdx = diaryIdx;
        this.emotionIdx = emotionIdx;
        this.diaryDate = diaryDate;
        this.content = content;
    }

}
