package com.umc.btos.src.mailbox.model;

import com.umc.btos.src.diary.model.GetDoneRes;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetDiaryRes_Mailbox {
    private int diaryIdx;
    private int emotionIdx;
    private String diaryDate;
    private int isPublic;
    private String content;
    private List<GetDoneRes> doneList;
    private int senderFontIdx; // 발송자 폰트 정보

    public GetDiaryRes_Mailbox(int diaryIdx, int emotionIdx, String diaryDate, int isPublic, String content) {
        this.diaryIdx = diaryIdx;
        this.emotionIdx = emotionIdx;
        this.diaryDate = diaryDate;
        this.isPublic = isPublic;
        this.content = content;
    }

}
