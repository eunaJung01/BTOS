package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Reply {
    private int replyIdx;
    private String senderNickName;
    private String content;
    private String sendAt; // yyyy.MM.dd
}
