package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class History<T> implements Comparable<History> {
    private String type; // diary : 일기 / letter : 편지 / reply : 답장
    private int idx; // 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
    private String senderNickName; // 발신자 이름
    private T content; // 내용 (diary : History_Diary / letter, reply : String)
    private String sendAt_raw; // 수신일 (yyyy-MM-dd HH:mm:ss)
    private String sendAt; // 수신일 - 화면 출력용 (yyyy.MM.dd)

    @SneakyThrows
    @Override
    public int compareTo(History history) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // ex. 2022-01-20 14:03:23
        Date date1 = format.parse(sendAt_raw);
        Date date2 = format.parse(history.getSendAt_raw());

        if (date1.before(date2)) {
            return 1;
        } else {
            return -1;
        }
    }

}
