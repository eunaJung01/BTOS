package com.umc.btos.src.archive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetDiaryRes {
    private int diaryIdx;
    private int emotionIdx;
    private int isPublic;
    private String diaryDate;
    private String content;
    private List<String> doneList;
}
