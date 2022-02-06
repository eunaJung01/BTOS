package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FirstHistory {
    private int idx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String content; // 내용 - createdAt 기준 내림차순 정렬 시 첫 항목
    private int emotionIdx = 0; // 일기일 경우 감정 이모티콘이 없다면 0, 아니면 1~8 / 편지 또는 답장일 경우 0
    private List<Done> doneList = null; // 일기일 경우 done list 배열 / 편지 또는 답장일 경우 null
    private String senderNickName;
    private String sendAt; // 발신일 (yyyy.MM.dd)
    private boolean positioning = false;

    public FirstHistory(int idx, String content, int emotionIdx, String senderNickName, String sendAt) {
        this.idx = idx;
        this.content = content;
        this.emotionIdx = emotionIdx;
        this.senderNickName = senderNickName;
        this.sendAt = sendAt;
    }

    public FirstHistory(int idx, String content, String senderNickName, String sendAt) {
        this.idx = idx;
        this.content = content;
        this.senderNickName = senderNickName;
        this.sendAt = sendAt;
    }

}
