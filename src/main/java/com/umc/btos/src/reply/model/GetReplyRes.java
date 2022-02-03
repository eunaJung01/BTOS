package com.umc.btos.src.reply.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetReplyRes {
    private int replyIdx;
    private int replierIdx;
    private int receiverIdx;
    private int isChecked; // default = 0
    private String firstHistoryType; // 시작점 구분 // diary : 일기, letter : 편지
    private int sendIdx; // DiarySendList.sendIdx, LetterSendList.sendIdx
    private String content;

}
