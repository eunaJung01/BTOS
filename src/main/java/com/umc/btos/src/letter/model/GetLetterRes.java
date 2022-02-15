package com.umc.btos.src.letter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetLetterRes {
    private int letterIdx;
    private String content; // 조회된 편지의 내용
}
