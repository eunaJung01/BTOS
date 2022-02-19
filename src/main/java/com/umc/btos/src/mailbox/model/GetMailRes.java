package com.umc.btos.src.mailbox.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetMailRes {
    private String firstHistoryType; // 시작점 type (diary : 일기 / letter : 편지)
    private String type; // 현재 조회하는 우편 type
    private int typeIdx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String content; // 내용
    private int emotionIdx = 0; // 일기일 경우 감정 이모티콘이 없다면 0, 아니면 1~8 / 편지 또는 답장일 경우 0
    private List<String> doneList = null; // 일기일 경우 done list 배열 / 편지 또는 답장일 경우 null
//    private String diaryDate; // 일기일 경우 일기 작성일 / 편지 또는 답장일 경우 null
    private String sendAt; // 일기일 경우 일기 작성일(== 발신일) / 편지 또는 답장일 경우 수신일(== 발신일) (yyyy.MM.dd)
    private String senderNickName; // 발신인 User.nickName
    private boolean senderActive; // 발신인 계정 상태 1. true : 활성 또는 휴면 2. false : 탈퇴 -> 답장 불가
    private int senderFontIdx; // 발신인 User.fontIdx

    // type = diary
    public GetMailRes(String firstHistoryType, String type, int typeIdx, String content, int emotionIdx, String sendAt, String senderNickName, int senderFontIdx) {
        this.firstHistoryType = firstHistoryType;
        this.type = type;
        this.typeIdx = typeIdx;
        this.content = content;
        this.emotionIdx = emotionIdx;
        this.sendAt = sendAt;
        this.senderNickName = senderNickName;
        this.senderFontIdx = senderFontIdx;
    }

    // type = letter, reply
    public GetMailRes(String firstHistoryType, String type, int typeIdx, String content, String sendAt, String senderNickName, int senderFontIdx) {
        this.firstHistoryType = firstHistoryType;
        this.type = type;
        this.typeIdx = typeIdx;
        this.content = content;
        this.sendAt = sendAt;
        this.senderNickName = senderNickName;
        this.senderFontIdx = senderFontIdx;
    }

}
