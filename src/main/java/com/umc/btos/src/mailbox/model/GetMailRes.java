package com.umc.btos.src.mailbox.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetMailRes<T> {
    private T content; // 내용 (GetDiaryRes / GetLetterRes / GetReplyRes)
    private String senderNickName; // 발송자 닉네임
    private int senderFontIdx; // 발송자 폰트 정보
}
