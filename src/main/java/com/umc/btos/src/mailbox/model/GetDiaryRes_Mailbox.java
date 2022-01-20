package com.umc.btos.src.mailbox.model;

import com.umc.btos.src.diary.model.GetDiaryRes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetDiaryRes_Mailbox {
    GetDiaryRes diary;
    private int senderFontIdx; // 발송자 폰트 정보

    public GetDiaryRes_Mailbox(GetDiaryRes getDiaryRes) {
        this.diary = getDiaryRes;
    }

}
