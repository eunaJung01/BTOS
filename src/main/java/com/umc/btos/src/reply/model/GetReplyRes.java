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
    private int isChecked;
    private String firstType;
    private String content;

}
