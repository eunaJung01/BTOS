package com.umc.btos.src.diary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostDiaryReq {
    private int userIdx;
    private int emotionIdx;
    private String diaryDate; // yyyy.MM.dd
    private String diaryContent;
    private boolean isPublic;
    private List<String> doneList;

    public int getIsPublic_int() { // https://projectlombok.org/features/GetterSetter
        if (this.isPublic) {
            return 1; // isPublic = true -> 당일 발송
        } else {
            return 0; // isPublic = false
        }
    }

}
