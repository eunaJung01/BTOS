package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// History 본문 보기
public class History_Main {
    private String type; // diary : 일기 / letter : 편지 / reply : 답장
    private int typeIdx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String content; // 내용 - createdAt 기준 내림차순 정렬 시 첫 항목
    private int emotionIdx = 0; // 일기일 경우 감정 이모티콘이 없다면 0, 아니면 1~8 / 편지 또는 답장일 경우 0
    private List<Done> doneList = null; // 일기일 경우 done list 배열 / 편지 또는 답장일 경우 null
    private String senderNickName;
    private String sendAt; // 발신일 (yyyy.MM.dd)
    private boolean positioning = false;

    // type = diary
    public History_Main(String type, int typeIdx, String content, int emotionIdx, String senderNickName, String sendAt) {
        this.type = type;
        this.typeIdx = typeIdx;
        this.content = content;
        this.emotionIdx = emotionIdx;
        this.senderNickName = senderNickName;
        this.sendAt = sendAt;
    }

    // type = letter or reply
    public History_Main(String type, int typeIdx, String content, String senderNickName, String sendAt) {
        this.type = type;
        this.typeIdx = typeIdx;
        this.content = content;
        this.senderNickName = senderNickName;
        this.sendAt = sendAt;
    }

}