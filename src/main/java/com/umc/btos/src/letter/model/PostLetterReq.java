package com.umc.btos.src.letter.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLetterReq {

    private int letterIdx;
    private int replierIdx;
    private int receiverIdx;
    private String content;

}
