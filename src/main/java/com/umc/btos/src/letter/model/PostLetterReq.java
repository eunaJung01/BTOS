package com.umc.btos.src.letter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostLetterReq {
    private int userIdx; // 발신인 식별자
    private String content; // 편지 내용
}
