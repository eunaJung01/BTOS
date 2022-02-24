package com.umc.btos.src.notice.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostNoticeReq {
    private String title; // 제목
    private String content; // 내용
}
