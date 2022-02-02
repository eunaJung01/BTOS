package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Letter {
    private int letterIdx;
    private String content;
    private String senderNickName;
    private String sendAt; // yyyy.MM.dd
    private boolean positioning = false;

    public Letter(int letterIdx, String content, String senderNickName, String sendAt) {
        this.letterIdx = letterIdx;
        this.content = content;
        this.senderNickName = senderNickName;
        this.sendAt = sendAt;
    }

}
