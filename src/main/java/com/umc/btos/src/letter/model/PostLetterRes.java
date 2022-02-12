package com.umc.btos.src.letter.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor

public class PostLetterRes {

    private int letterIdx;
    private List<Integer> receiveUserIdx; //전송한 유저idx들
}
