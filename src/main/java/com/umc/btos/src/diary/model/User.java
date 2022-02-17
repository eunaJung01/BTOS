package com.umc.btos.src.diary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int userIdx; // 회원 식별자
    private int userIdx_recentReceived; // 가장 최근에 수신한 편지의 발신인 userIdx

    public User(int userIdx) {
        this.userIdx = userIdx;
    }
}