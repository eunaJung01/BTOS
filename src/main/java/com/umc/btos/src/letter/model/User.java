package com.umc.btos.src.letter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int userIdx;
    private int birth;
    private int recSimilarAge;
    private int userIdx_recentReceived; // 가장 최근에 수신한 편지의 발신인 userIdx

    public User(int userIdx, int birth, int recSimilarAge) {
        this.userIdx = userIdx;
        this.birth = birth;
        this.recSimilarAge = recSimilarAge;
    }
}
