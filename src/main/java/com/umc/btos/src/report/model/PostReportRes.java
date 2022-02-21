package com.umc.btos.src.report.model;

import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class PostReportRes {
    private int reportIdx;
    private String type; // 신고 구분 (diary : 일기 / letter : 편지 / reply : 답장)
    private int reportedUserIdx; // 신고를 당한 회원 식별자 (userIdx)
    private PatchModifyScoreRes patchModifyScoreRes; // 신고를 당한 회원의 화분 점수 변경 결과
}
