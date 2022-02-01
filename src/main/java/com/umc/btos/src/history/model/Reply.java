package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reply {
    private int replyIdx;
    private String senderNickName;
    private String content;
    private String sendAt; // yyyy.MM.dd
    private boolean positioning = false;

    public Reply(int replyIdx, String content, String senderNickName, String sendAt) {
        this.replyIdx = replyIdx;
        this.content = content;
        this.senderNickName = senderNickName;
        this.sendAt = sendAt;
    }

}
