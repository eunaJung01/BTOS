package com.umc.btos.src.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

// 자동 로그인 response 모델
public class GetAuthLoginRes {
    private int userIdx;
}
