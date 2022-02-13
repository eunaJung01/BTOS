package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
// History 본문 보기
public class GetHistoryRes {
    private String type; // diary : 일기 / letter : 편지 / reply : 답장
    private int typeIdx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String content; // 내용 - createdAt 기준 내림차순 정렬 시 첫 항목
    private int emotionIdx = 0; // 일기일 경우 감정 이모티콘이 없다면 0, 아니면 1~8 / 편지 또는 답장일 경우 0
    private List<Done> doneList = null; // 일기일 경우 done list 배열 / 편지 또는 답장일 경우 null
    private String sendAt_raw; // 발신일(== 수신일) (yyyy-MM-dd HH:mm:ss)
    private String sendAt; // 발신일 (yyyy.MM.dd)
    private String senderNickName; // 발신인 User.senderNickName
    private boolean senderActive; // 발신인 계정 상태 1. true : 활성 또는 휴면 2. false : 탈퇴 -> 답장 불가
    private int senderFontIdx; // 발신인 User.fontIdx
    private boolean positioning = false;

    // type = diary
    public GetHistoryRes(String type, int typeIdx, String content, int emotionIdx, String senderNickName, String sendAt_raw, String sendAt, boolean senderActive, int senderFontIdx) {
        this.type = type;
        this.typeIdx = typeIdx;
        this.content = content;
        this.emotionIdx = emotionIdx;
        this.sendAt_raw = sendAt_raw;
        this.sendAt = sendAt;
        this.senderNickName = senderNickName;
        this.senderActive = senderActive;
        this.senderFontIdx = senderFontIdx;
    }

    // type = letter, reply
    public GetHistoryRes(String type, int typeIdx, String content, String sendAt_raw, String sendAt, String senderNickName, boolean senderActive, int senderFontIdx) {
        this.type = type;
        this.typeIdx = typeIdx;
        this.content = content;
        this.sendAt_raw = sendAt_raw;
        this.sendAt = sendAt;
        this.senderNickName = senderNickName;
        this.senderActive = senderActive;
        this.senderFontIdx = senderFontIdx;
    }

}
