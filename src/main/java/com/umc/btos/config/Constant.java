package com.umc.btos.config;

public class Constant {
    public static final int DIARYLIST_DATA_NUM = 20; // Archive 조회 - 일기 리스트

    //------------------------점수 증감 원인------------------------//
    public static final int PLANT_LEVELUP_DIARY = 5;
    public static final int PLANT_LEVELUP_LETTER = 3;
    public static final int PLANT_LEVELDOWN_DIARY = -10;
    public static final int PLANT_LEVELDOWN_REPORT_SEX_HATE = -100;
    public static final int PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE = -30;

    //----------------------단계 별 최대 점수----------------------//
//    public static final int PLANT_LEVEL_0 = 15;
//    public static final int PLANT_LEVEL_1 = 30;
//    public static final int PLANT_LEVEL_2 = 50;
//    public static final int PLANT_LEVEL_3 = 50;
//    public static final int PLANT_LEVEL_4 = 70;
//    public static final int PLANT_LEVEL_5 = 70;

    // 단계 별 성장치 [LEVEL_0, LEVEL_1, LEVEL_2, LEVEL_3]
    public static final int[] PLANT_LEVEL = new int[] {15, 30, 50, 50};

}
