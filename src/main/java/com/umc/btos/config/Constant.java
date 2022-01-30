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

}
