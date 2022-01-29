package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class HistoryList_Sender {
    private String senderNickName; // 발신자 이름
    private int historyListNum; // 개수
    private List<History> historyList; // 내용

    public HistoryList_Sender(String senderNickName, List<History> historyList) {
        this.senderNickName = senderNickName;
        this.historyList = historyList;
    }

}
