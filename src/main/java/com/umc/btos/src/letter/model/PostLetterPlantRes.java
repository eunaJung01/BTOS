package com.umc.btos.src.letter.model;

import com.umc.btos.src.plant.model.PatchModifyScoreRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostLetterPlantRes {
    private int letterIdx;
    private String senderNickName; //편지를 보낸 사람의 닉네임
    private List<Integer> receiverIdxList; //전송한 유저idx들
    private PatchModifyScoreRes patchModifyScoreRes; // 화분점수 변경 반환

}
