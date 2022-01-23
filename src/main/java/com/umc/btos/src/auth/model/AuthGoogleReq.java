package com.umc.btos.src.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

// 소셜 로그인 시 받을 body 값
public class AuthGoogleReq {
    private String email;
}
