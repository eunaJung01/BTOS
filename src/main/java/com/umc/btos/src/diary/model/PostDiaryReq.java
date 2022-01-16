package com.umc.btos.src.diary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostDiaryReq {
    private int userIdx;
    private int emotionIdx;
    private String diaryDate; // YYYY-MM-DD
    private String diaryContent;
    private int isPublic;
    private List doneList;
}
