package com.umc.btos.src.letter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Receiver {
    private int userIdx; // 수신인 식별자
    private String fcmToken; // 수신인 fcm token

    public Receiver(int userIdx) {
        this.userIdx = userIdx;
    }
}
