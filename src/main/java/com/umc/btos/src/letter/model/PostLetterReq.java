package com.umc.btos.src.letter.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLetterReq {

    private int letterIdx;
    private int userIdx; // 편지를 작성하는 유저의 userIdx
    private String content; // 작성한 편지의 내용

}
