package com.umc.btos.src.letter.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLetterSendListReq {
    private int sendIdx;
    private int letterIdx;
    private int receiverIdx;

}
