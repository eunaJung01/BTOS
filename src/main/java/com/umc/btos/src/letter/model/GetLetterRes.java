package com.umc.btos.src.letter.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetLetterRes {
    private int letterIdx;
    private int userIdx; // 전송하는 사람
    private int receiverIdx; // 전송받는 사람
    private String content;


}
