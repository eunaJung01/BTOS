package com.umc.btos.src.reply.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostReplyRes {
    private int replyIdx; // 답장 식별자
    private String senderNickName; // 발신인 닉네임
    private int receiverIdx; // 수신인 userIdx
    private String fcmToken; // 수신인 fcm token
}
