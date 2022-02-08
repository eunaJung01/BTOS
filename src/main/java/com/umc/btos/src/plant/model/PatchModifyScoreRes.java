package com.umc.btos.src.plant.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatchModifyScoreRes {
    private String type; // 1. diary / 2. letter / 3. report_sex_hate / 4. report_spam_dislike / 5.report_etc
    private String status = null; // 단계 증가 : levelUp / 단계 감소 : levelDown / 최대 단계 & 최대 점수인 경우라 변동 없을 때, 점소 감수 시 음수인 경우(0단계 0점) : null
    private boolean levelChanged; // 단계 변경되었으면 true, 아니면 false

    public PatchModifyScoreRes(boolean levelChanged, String type) {
        this.type = type;
        this.levelChanged = levelChanged;
    }

}
