package com.umc.btos.src.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//인증 코드 받고 교환 받을 때 사용
//구글에게 받을 응답 모델
public class AuthGoogleRes {
    private int accessToken;
    private String expiresIn;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private String idToken;
}
