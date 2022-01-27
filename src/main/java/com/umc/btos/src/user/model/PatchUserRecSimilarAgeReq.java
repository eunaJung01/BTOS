package com.umc.btos.src.user.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class PatchUserRecSimilarAgeReq {
    private int userIdx;
    private boolean recSimilarAge;
}
