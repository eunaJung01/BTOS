package com.umc.btos.config;

public class Constant {
    // 페이징 처리 - 한 요청에 보낼 데이터의 개수
    public static final int DIARYLIST_DATA_NUM = 20; // Archive 조회 - 일기 리스트
    public static final int HISTORY_DATA_NUM = 20; // History 목록 조회

    // 화분 - 점수 증감 원인
    public static final int PLANT_LEVELUP_DIARY = 5;
    public static final int PLANT_LEVELUP_LETTER = 3;
    public static final int PLANT_LEVELDOWN_REPORT_SEX_HATE = -100;
    public static final int PLANT_LEVELDOWN_REPORT_SPAM_DISLIKE = -30;

    // 화분 - 단계 별 성장치 [LEVEL_0, LEVEL_1, LEVEL_2, LEVEL_3]
    public static final int[] PLANT_LEVEL = new int[] {20, 30, 50, 50};

    // 일기 & 편지 - 비슷한 나이대 기준 (-n ~ +n)
    public static final int SIMILAR_AGE_STANDARD = 5;

    // 일기 - 비슷한 나이대로 수신할 수 있는 회원의 최대 비율 (%)
    public static final int DIARY_REC_SIMILAR_AGE_RATIO = 80;

    // 편지 - 편지를 발송해야 하는 총 횟수
    public static final int LETTER_SEND_TOTAL_NUM = 5;

    // 편지 - 비슷한 나이대로 발송할 최대 횟수
    public static final int LETTER_SEND_SIMILAR_AGE_NUM = 3;

    // 시스템 메일
    public static final String SYSTEM_MAIL_GREETINGS = "저편너머 세계에 오신것을 환영합니다 ";
    public static final String SYSTEM_MAIL_MAIN =
            "\n" +
            "\n이곳에서는 오늘 일어난 일어난 지극히 일상적인, 혹은 특별할지도 모르는 일들을 다른 사람들과 공유할 수 있어요.\n" +
            "\n" +
            "오늘 하루동안 느낀 감정, 한 일들을 정리하고, 일기를 작성해 보세요. 공개 설정을 하게 되면 내일의 누군가가 내 일기를 받아볼 수도 있답니다.\n" +
            "혹시 아나요? 나에게는 일상적인 일들이, 다른 사람들에게는 특별하게 다가오는 일상의 자극제가 될 수도 있을지...\n" +
            "일기가 마음에 들었다면 답장을 주고 받으며 친구로 발전할 수도 있답니다 :)\n" +
            "\n" +
            "굳이 일기가 아니어도 돼요! 어딘가에 털어놓고 싶은 말이 있다면, 언제든지 우편함 아래에 있는 버튼을 눌러서 저편너머에 있는 누군가에게 편지를 보내보세요.\n" +
            "때로는 모르는 사람에게서 얻는 위로가 큰 힘이 되기도 한답니다.\n" +
            "\n" +
            "알로카시아 씨앗은 잘 심으셨나요? 매일 일기를 써서 시들지 않게 잘 관리해 주세요.\n" +
            "이미 지나버린 과거는 오늘의 알로카시아에게 아무런 힘을 주지 못합니다. 오늘 작성한 일기만이 효력을 발휘한다는 점 꼭 기억해주세요!!\n" +
            "\n" +
            "- 저편너머에서 온 편지 -\n";

    // 알림 title
    public static final String LETTER_TITLE = "편지 도착!";
    public static final String LETTER_BODY = "님의 편지가 도착했습니다.";

    public static final String REPLY_TITLE = "답장 도착!";
    public static final String REPLY_BODY = "님의 답장이 도착했습니다.";

}
