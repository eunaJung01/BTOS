package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HistoryList_Sender {
    private String senderNickName; // 발신자 이름
    private int historyListNum; // 수신한 일기와 편지들 총 개수
    private History_Sender firstContent; // 내용 - createdAt 기준 내림차순 정렬 시 상위 1번째 항목
}
