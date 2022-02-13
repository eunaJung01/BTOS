package com.umc.btos.src.reply.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetReplyRes {
    private int replyIdx;
    private String content; // 답장의 내용
}
