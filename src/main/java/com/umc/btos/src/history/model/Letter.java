package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Letter {
    private int letterIdx;
    private String content;
    private String senderNickName;
    private String sendAt; // yyyy.MM.dd
}
