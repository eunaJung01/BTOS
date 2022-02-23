package com.umc.btos.src.suggest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostSuggestReq {
    private int userIdx; // 회원 식별자
    private String type; // 건의 구분 (bug : 버그 제보 / user : 악성 유저 신고 / add : 추가되었으면 하는 기능 / etc : 기타)
    private String content; // 내용
}
