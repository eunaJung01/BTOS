package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
// History 목록 조회 (filtering = sender)
public class HistoryList_Sender {
    private String senderNickName; // 발신자 이름
    private int historyListNum; // 수신한 일기, 편지, 답장의 전체 개수
    private History firstContent; // 내용 - createdAt 기준 내림차순 정렬 시 상위 1번째 항목
}
