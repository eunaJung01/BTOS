package com.umc.btos.src.letter.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostLetterUserSimilarIdx {
    private int userIdx; // 편지를 보낸 이의 userIdx
    private int userSimilarAge; // 편지를 보낸이의 userSimilarAge의 여부 // 1일경우 True, 0일경우 False
}
