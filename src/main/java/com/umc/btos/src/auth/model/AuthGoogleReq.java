package com.umc.btos.src.auth.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// 소셜 로그인 시 받을 body 값
public class AuthGoogleReq {
    private String email;
}
