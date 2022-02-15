package com.umc.btos.src.letter.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostLetterReq {
    private int letterIdx;
    private int userIdx;
    private String content;
}
