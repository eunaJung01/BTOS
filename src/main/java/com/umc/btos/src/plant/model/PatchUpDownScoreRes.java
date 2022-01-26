package com.umc.btos.src.plant.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

/*
- 단계 증감 여부
- 점수, 단계 증감 이유

<반환값>
default = 0

해당하면 1로 set
 */

public class PatchUpDownScoreRes {
    // ---------------- 단계 증감 여부 ----------------//
    private int upLevel;
    private int downLevel;

    // ---------------- 점수, 단계 증감 이유 ----------------//
    private int diary_upScore;
    private int letter_upScore;
    private int diary_downScore;
    private int report_sex_hate_downScore;
    private int report_spam_dislike_downScore;

    public PatchUpDownScoreRes() { //default(0)값으로 초기화
        diary_upScore = 0;
        upLevel = 0;
        downLevel = 0;
        letter_upScore = 0;
        diary_downScore = 0;
        report_sex_hate_downScore = 0;
        report_spam_dislike_downScore = 0;
    }
}
