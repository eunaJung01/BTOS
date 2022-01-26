package com.umc.btos.src.mailbox.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetMailRes<T> {
    private String type; // diary : 일기 / letter : 편지 / reply : 답장
    private T content; // 내용 (GetDiaryRes / GetLetterRes / GetReplyRes)
    private int senderFontIdx; // 발송자 폰트 정보

    public GetMailRes(String type) {
        this.type = type;
    }

}
