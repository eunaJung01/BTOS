package com.umc.btos.src.mailbox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetMailRes<T> {
    String type; // 일기(diary), 편지(letter), 답장(reply)
    T content; // 내용
    private int senderFontIdx; // 발송자 폰트 정보

    public GetMailRes(String type) {
        this.type = type;
    }

}
