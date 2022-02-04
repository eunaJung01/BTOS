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
    private int isPublic;
    private String content;
    private List<GetDoneRes> doneList;

    public GetDiaryRes(int diaryIdx, int emotionIdx, String diaryDate, int isPublic, String content) {
        this.diaryIdx = diaryIdx;
        this.emotionIdx = emotionIdx;
        this.diaryDate = diaryDate;
        this.isPublic = isPublic;
        this.content = content;
    }

}
