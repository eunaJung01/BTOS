package com.umc.btos.src.mailbox.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetMailRes<T> {
    private String firstHistoryType; // 답장의 시작점 type (diary : 일기 / letter : 편지)
    private T mail; // 내용 (GetDiaryRes / GetLetterRes / GetReplyRes)
    private String senderNickName; // 발송자 닉네임
    private int senderFontIdx; // 발송자 폰트 정보
}
