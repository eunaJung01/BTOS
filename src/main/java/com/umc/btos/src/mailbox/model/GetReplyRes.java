package com.umc.btos.src.mailbox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetReplyRes {
    private int replyIdx;
    private int replierIdx;
    private int receiverIdx;
    private String content;
}
