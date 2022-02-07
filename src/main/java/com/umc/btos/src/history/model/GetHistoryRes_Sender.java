package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// History 발신인 조회
public class GetHistoryRes_Sender {
    private String senderNickName; // 발신자 이름
    private List<History_Sender> historyList; // 내용

    public GetHistoryRes_Sender(String senderNickName) {
        this.senderNickName = senderNickName;
    }

}
