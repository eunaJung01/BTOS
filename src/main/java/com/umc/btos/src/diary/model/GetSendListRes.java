package com.umc.btos.src.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetSendListRes {
    private int diaryIdx;
    private String senderNickName;
    private List<Integer> receiverIdxList;

    public GetSendListRes(int diaryIdx, String senderNickName) {
        this.diaryIdx = diaryIdx;
        this.senderNickName = senderNickName;
    }

}
