package com.umc.btos.src.user.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class PatchUserIsSadReq {
    private int userIdx;
    private boolean isSad;

    public boolean isIsSad(){
        return this.isSad;
    }
}
