package com.umc.btos.src.reply.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostReplyRes {
    private int receiverIdx; // 답장을 받는 사람의 userIdx
    private String senderNickName; // 답장을 보내는 사람의 닉네임
}
