package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Diary {
    private int diaryIdx;
    private int emotionIdx;
    private String content;
    private List<Done> doneList;
    private String senderNickName;
    private String sendAt; // yyyy.MM.dd

    public Diary(int diaryIdx, int emotionIdx, String content, String senderNickName, String sendAt) {
        this.diaryIdx = diaryIdx;
        this.emotionIdx = emotionIdx;
        this.content = content;
        this.senderNickName = senderNickName;
        this.sendAt = sendAt;
    }

}
