package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetSenderRes {
    private String senderNickName; // 발신자 이름
    private List<History> historyList; // 내용

    public GetSenderRes(String senderNickName) {
        this.senderNickName = senderNickName;
    }

}
