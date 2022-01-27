package com.umc.btos.src.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor


// 소셜 로그인 결과
// -> 신규/탈퇴 회원인 경우 회원가입 메시지
// -> 휴면 회원일 경우 상태변경 메시지
// -> 기존 회원일 경우 res jwt
public class AuthGoogleRes {
    private int userIdx;
    private String jwt;
}
