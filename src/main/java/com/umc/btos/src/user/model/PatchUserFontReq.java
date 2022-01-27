package com.umc.btos.src.user.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class PatchUserFontReq {
    private int userIdx;
    private int fontIdx;
}
