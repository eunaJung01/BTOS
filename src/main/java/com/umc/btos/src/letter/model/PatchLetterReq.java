package com.umc.btos.src.letter.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatchLetterReq {
    private int letterIdx;
}
