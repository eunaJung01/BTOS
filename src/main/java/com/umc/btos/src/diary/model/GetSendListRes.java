package com.umc.btos.src.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetSendListRes {
    private int diaryIdx;
    private List<Integer> receiverIdxList;

    public GetSendListRes(int diaryIdx) {
        this.diaryIdx = diaryIdx;
    }

}
