package com.umc.btos.src.reply.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostReplyFinalRes {
    private int replyIdx;
    private int receiverIdx; // 편지를 받는 사람의 userIdx
    private String SenderNickName;
}
