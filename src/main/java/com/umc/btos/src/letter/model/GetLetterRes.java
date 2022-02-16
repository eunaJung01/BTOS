package com.umc.btos.src.letter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetLetterRes {
    private int letterIdx; // 조회하는 편지의 식별자
    private String content; // 편지 내용
}
