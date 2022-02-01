package com.umc.btos.src.archive.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetDiaryListRes {
    private String month;
    private List<Diary> diaryList;
}
