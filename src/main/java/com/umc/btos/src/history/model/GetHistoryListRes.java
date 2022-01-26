package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetHistoryListRes<T> {
    private String type; // diary : 일기 / letter : 편지
    private int idx; // 식별자 (diary - diaryIdx / letter - letterIdx)
    private String senderNickName; // 발신자 이름
    private T content; // 내용 (diary : GetDiaryRes_History / letter : String)
    private String sendAt; // yyyy.MM.dd
}
