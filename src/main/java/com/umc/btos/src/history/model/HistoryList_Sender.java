package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HistoryList_Sender {
    private String senderNickName; // 발신자 이름
    private int historyListNum; // 개수
    private History firstContent; // 내용 (createdAt 기준 내림차순 정렬 시 가장 윗 항목)
}
