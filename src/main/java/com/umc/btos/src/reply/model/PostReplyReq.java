package com.umc.btos.src.reply.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReplyReq {
    private int replierIdx; // 발신인 userIdx
    private int receiverIdx; // 수신인 userIdx
    private String firstHistoryType; // 답장의 시작점 type (diary : 일기 / letter : 편지)
    private int sendIdx; // 전송 테이블 식별자 (1. type = diary -> DiarySendList.sendIdx / 2. type = letter -> LetterSendList.sendIdx)
    private String content; // 답장 내용
}
