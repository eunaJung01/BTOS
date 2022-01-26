package com.umc.btos.src.reply.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReplyReq {
    private int replyIdx;
    private int replierIdx;
    private int receiverIdx;
    private int isChecked;
    private String firstType;
    private String content;


}
