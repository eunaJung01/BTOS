package com.umc.btos.src.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

//승인 요청 시 사용
//구글에게 요청 매개변수 모델
public class GetAuthGoogleReq {
    //요청 필수 매개변수
    private String redirectUri;
    private String clientId;
    private String responseType;
    private String scope;
    //구글 애플리케이션 설정 시 선택한 범위, 스코프에 따라 받을 수 있는 유저 정보가 다름
    private String code;

    private String clientSecret;
    private String accessType;
    private String state;
    private String includeGrantedScopes;
    private String loginHint;
    private String prompt;
}
