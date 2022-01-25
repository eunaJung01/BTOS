package com.umc.btos.src.mailbox.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetMailboxRes {
    private String type; // diary : 일기 / letter : 편지 / reply : 답장
    private int idx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String senderName; // 발신자 이름
    private boolean hasSealing; // 일기 : 0 (실링 X) / 편지, 답장 : 1 (실링 O)
}
