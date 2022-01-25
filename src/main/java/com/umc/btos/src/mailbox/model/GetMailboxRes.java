package com.umc.btos.src.mailbox.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class GetMailboxRes implements Comparable<GetMailboxRes> {
    private String type; // diary : 일기 / letter : 편지 / reply : 답장
    private int idx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String senderNickName; // 발신자 이름
    private String sendAt; // 수신일(yyyy-MM-dd HH:mm:ss) : Diary.updatedAt / Letter.updatedAt (매일 19시 발송) / Reply.createdAt
    private boolean hasSealing; // 일기 : true (실링 O) / 편지, 답장 : false (실링 X)

    @SneakyThrows
    @Override
    public int compareTo(GetMailboxRes mailbox) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // ex. 2022-01-20 14:03:23
        Date date1 = format.parse(sendAt);
        Date date2 = format.parse(mailbox.getSendAt());

        if (date1.before(date2)) {
            return 1;
        } else {
            return -1;
        }
    }

}
