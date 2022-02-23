package com.umc.btos.src.notice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetNoticeRes {
    private int noticeIdx;
    private String title; // 제목
    private String content; // 내용
    private String createdAt; // 공지 작성일
}
