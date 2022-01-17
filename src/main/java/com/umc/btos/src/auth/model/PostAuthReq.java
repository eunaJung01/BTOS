package com.umc.btos.src.auth.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// 회원가입 시 필요한 정보(클라에서 보내주는 정보)
public class PostAuthReq {
    private String email;
    private String nickName;
    private int birth;
}
