package com.umc.btos.src.mailbox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetMailboxRes {
    private String type; // diary : 일기 / letter : 편지 / reply : 답장
    private int idx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String senderNickName; // 발신자 이름
    private String sendDate; // 발신일
    private boolean hasSealing; // 일기 : true (실링 O) / 편지, 답장 : false (실링 X)
}
