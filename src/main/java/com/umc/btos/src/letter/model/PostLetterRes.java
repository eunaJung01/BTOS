package com.umc.btos.src.letter.model;

import com.umc.btos.src.plant.model.PatchModifyScoreRes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostLetterRes {
    private int letterIdx; // 편지 식별자
    private String senderNickName; // 발신인 닉네임
    private List<Receiver> receiverList; // 수신인 목록
    private PatchModifyScoreRes plantRes; // 편지를 발송 후 해당 회원의 화분에 대한 정보 (점수 및 단계 증가)

    public PostLetterRes(int letterIdx, String senderNickName, List<Receiver> receiverList) {
        this.letterIdx = letterIdx;
        this.senderNickName = senderNickName;
        this.receiverList = receiverList;
    }

}
