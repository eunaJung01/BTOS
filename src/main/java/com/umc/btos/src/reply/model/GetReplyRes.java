package com.umc.btos.src.reply.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetReplyRes {
    private int replyIdx; // 답장 식별자
    private String content; // 답장 내용
}
