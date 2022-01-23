package com.umc.btos.src.user.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class PatchUserInfoReq {
    private int userIdx;
    private String nickName;
    private int birth;
}
