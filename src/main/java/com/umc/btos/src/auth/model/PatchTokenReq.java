package com.umc.btos.src.auth.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// 디바이스 토큰 갱신 시 받을 body 값
public class PatchTokenReq {
    private String fcmToken;
}
