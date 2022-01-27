package com.umc.btos.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

// 회원가입 완료 시 보내주는 정보
public class PostUserRes {
    private int userIdx;
}
