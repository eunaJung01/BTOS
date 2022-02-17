package com.umc.btos.src.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetDiaryRes {
    private int diaryIdx;
    private int emotionIdx;
    private String diaryDate; // yyyy.MM.dd
    private String content;
    private List<Done> doneList;

    public GetDiaryRes(int diaryIdx, int emotionIdx, String diaryDate, String content) {
        this.diaryIdx = diaryIdx;
        this.emotionIdx = emotionIdx;
        this.diaryDate = diaryDate;
        this.content = content;
    }

}
