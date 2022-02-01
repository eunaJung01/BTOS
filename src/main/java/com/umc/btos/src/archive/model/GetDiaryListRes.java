package com.umc.btos.src.archive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetDiaryListRes {
    private String month;
    private List<Diary> diaryList;
}
