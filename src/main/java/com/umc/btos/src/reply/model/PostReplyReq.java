package com.umc.btos.src.reply.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostReplyReq {
    private int replierIdx; // 발신인 userIdx
    private int receiverIdx; // 수신인 userIdx
    private String firstHistoryType; // 답장의 시작점 type (diary : 일기 / letter : 편지)
    private int sendIdx; // DiarySendList.sendIdx, LetterSendList.sendIdx
    private String content; // 답장 내용
}
