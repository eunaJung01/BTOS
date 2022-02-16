package com.umc.btos.config;

public class Constant {
    // 페이징 처리 - 한 요청에 보낼 데이터의 개수
    public static final int DIARYLIST_DATA_NUM = 20; // Archive 조회 - 일기 리스트
    public static final int HISTORY_DATA_NUM = 20; // History 목록 조회

    // 화분 - 점수 증감 원인
    public static final int PLANT_LEVELUP_DIARY = 5;
    public static final int PLANT_LEVELUP_LETTER = 3;
    public static final int PLANT_LEVELDOWN_DIARY = -10;
    public static final int PLANT_LEVELDOWN_REPORT_SEX_HATE = -100;
    public static final int PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE = -30;

    // 화분 - 단계 별 성장치 [LEVEL_0, LEVEL_1, LEVEL_2, LEVEL_3]
    public static final int[] PLANT_LEVEL = new int[] {15, 30, 50, 50};

    // 일기 & 편지 - 비슷한 나이대 기준 (-n ~ +n)
    public static final int SIMILAR_AGE_STANDARD = 5;

    // 일기 - 비슷한 나이대로 수신할 수 있는 회원의 최대 비율 (%)
    public static final int DIARY_REC_SIMILAR_AGE_RATIO = 80;

    // 편지 - 편지를 발송해야 하는 총 횟수
    public static final int LETTER_SEND_TOTAL_NUM = 5;

    // 편지 - 비슷한 나이대로 발송할 최대 횟수
    public static final int LETTER_SEND_SIMILAR_AGE_NUM = 3;

}
