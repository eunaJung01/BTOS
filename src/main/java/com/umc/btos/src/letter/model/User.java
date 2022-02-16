package com.umc.btos.src.letter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int userIdx; // 회원 식별자
    private int birth; // 회원 생년
    private int recSimilarAge; // 비슷한 나이대 수신 여부 (수신에 동의한다면 1, 아니면 0)
    private int userIdx_recentReceived; // 가장 최근에 수신한 편지의 발신인 userIdx

    public User(int userIdx, int birth, int recSimilarAge) {
        this.userIdx = userIdx;
        this.birth = birth;
        this.recSimilarAge = recSimilarAge;
    }
}
