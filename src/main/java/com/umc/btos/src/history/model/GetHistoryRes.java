package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetHistoryRes<T> {
    private String firstType; // diary : 일기 / letter : 편지
    private T firstHistory; // firstType = 1. diary -> Diary 객체 / 2. letter -> Letter 객체
    private List<Reply> replyList;

    public GetHistoryRes(String firstType) {
        this.firstType = firstType;
    }

}
