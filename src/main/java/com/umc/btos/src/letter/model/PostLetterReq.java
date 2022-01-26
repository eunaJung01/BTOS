package com.umc.btos.src.letter.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLetterReq {

    private int letterIdx;
    private int userIdx;
    private String content;

}
