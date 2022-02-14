package com.umc.btos.src.report.model;


import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostReportUserIdxPlantRes {
    private int reportIdx;
    private String type; // 신고구분  // diary:일기, letter : 편지, reply : 답장
    private int reportedUserIdx; // 신고당한 유저의 Idx
    private PatchModifyScoreRes patchModifyScoreRes; // 화분점수 변경 반환

}
