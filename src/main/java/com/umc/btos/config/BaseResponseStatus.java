package com.umc.btos.config;

import lombok.Getter;

/*
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /*
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),


    /*
     * 2000 : Request 오류
     */
    // Common
    REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false, 2003, "권한이 없는 유저의 접근입니다."),

    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, 2016, "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false, 2017, "중복된 이메일입니다."),


    /*
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(false, 3014, "없는 아이디거나 비밀번호가 틀렸습니다."),


    /*
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userIdx}
    MODIFY_FAIL_USERNAME(false, 4014, "유저네임 수정 실패"),

    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다."),


    // 5000 : 도기
    TEMP1(false, 9000, "conflict 방지용 1"),
    POST_USERS_EMPTY_NICKNAME(false, 5000, "닉네임을 입력해주세요."),
    USERS_INVALID_NICKNAME(false, 5001, "닉네임은 10자까지 입력 가능합니다."),
    POST_USERS_DORMANT(false, 5002, "휴면 상태 계정입니다."),
    AUTH_REQ_SIGNUP(false, 5003, "회원가입이 필요합니다."),
    AUTH_FAILED_TO_LOGIN(false, 5004, "로그인에 실패하였습니다."),
    MODIFY_FAIL_STATUS(false, 5005, "상태 변경에 실패하였습니다."),
    INVALID_USER_BIRTH(false, 5006, "생년은 1 이상의 값만 가능합니다."),
    INVALID_USER_STATUS(false, 5007, "탈퇴, 활성화, 휴면 상태값만 가능합니다."),
    MODIFY_FAIL_INFO(false, 5008, "정보 변경에 실패하였습니다."),
    PATCH_USERS_EXISTS_NICKNAME(false, 5009, "이미 존재하는 닉네임입니다."),
    PATCH_USERS_NOT_VALUES(false, 5010, "변경하실 닉네임을 입력해주세요."),
    MODIFY_FAIL_PUSH_ALARM(false, 5011, "푸시 알람 수신 변경에 실패했습니다."),
    INVALID_FONT(false, 5012, "유효하지 않은 폰트입니다."),
    CHANGE_FAIL_FONT(false, 5013, "폰트 변경에 실패하였습니다."),
    MODIFY_FAIL_RECEIVE_OTHERS(false, 5014, "다른 사람의 편지 수신 변경에 실패하였습니다."),
    MODIFY_FAIL_RECEIVE_SIMILAR_AGE(false, 5015, "비슷한 연령대의 편지 수신 변경에 실패하였습니다."),
    CHANGE_FAIL_IS_SAD(false, 5016, "시무룩이 상태 변경에 실패하였습니다."),

    // 6000 : 레마
    TEMP2(false, 9000, "conflict 방지용 2"),
    INVALID_USERIDX(false, 6000, "존재하지 않는 회원입니다."),
    INVALID_DIARYIDX(false, 6001, "존재하지 않는 일기입니다."),
    INVALID_USER_ABOUT_DIARY(false, 6002, "해당 일기에 접근 권한이 없는 회원입니다."),
    DIARY_EXISTS(false, 6003, "일기는 하루에 하나만 작성 가능합니다."),
    UNPRIVATE_DATE(false, 6004, "당일에 작성한 일기만 발송 가능합니다!"),
    DIARY_ENCRYPTION_ERROR(false, 6005, "일기 또는 done list 내용 암호화에 실패하였습니다."),
    MODIFY_FAIL_DIARY(false, 6006, "일기 수정 실패 - 일기 내용"),
    MODIFY_FAIL_DONELIST(false, 6007, "일기 수정 실패 - done list"),
    DELETE_FAIL_DIARY(false, 6008, "일기 삭제 실패"),
    DELETE_FAIL_DONELIST(false, 6009, "done list 삭제 실패"),
    DIARY_NONPREMIUM_USER(false, 6010, "프리미엄 가입이 필요합니다."),
    DIARY_DECRYPTION_ERROR(false, 6011, "일기 복호화에 실패하였습니다."),
    EMPTY_RESULT(false, 6012, "검색 결과 없음"),
    PAGENUM_ERROR(false, 6013, "잘못된 페이지 요청입니다."),
    PAGENUM_ERROR_0(false, 6014, "페이지 번호는 1부터 시작합니다."),
    MAIN_HISTORY_WRONG_REQ(false, 6015, "잘못된 userIdx 또는 idx 요청입니다."),
    NO_DIARY_SENT_TODAY(false, 6016, "오늘 발송되는 일기는 없습니다."),


    // 7000 : 자몽
    TEMP3(false, 9000, "conflict 방지용 3"),

//    MODIFY_FAIL_STATUS(false, 7010, "화분 상태 변경에 실패하였습니다."),
    INSERT_FAIL_PLANT(false,7000,"해당 유저의 화분 초기화에 실패하였습니다."),

    MODIFY_FAIL_BUY_PLANT(false,7011, "화분 선택에 실패하였습니다."),
    MODIFY_FAIL_SCORE(false, 7012, "화분 점수 변경에 실패하였습니다."),
    MODIFY_FAIL_LEVEL(false, 7013, "화분 단계 변경에 실패하였습니다."),


    INVALID_SCORE_PLANT(false,7014, "선택한 화분의 점수가 0점입니다. 점수를 감소시킬 수 없습니다."),
    INVALID_LEVEL_PLANT(false,7015,"동작을 수행할 화분의 단계가 0단계이므로 더 이상 단계와 점수를 감소시킬 수 없습니다."),

    INVALID_IDX_PLANT(false, 7016, "이미 선택된 화분입니다."),
    DUPLICATE_IDX_PLANT(false, 7017, "이미 보유한 화분입니다."),

    MODIFY_FAIL_PREMIUM(false, 7020, "프리미엄 계정 변경에 실패하였습니다."),
    MODIFY_FAIL_WITHDRAW(false, 7021, "청약철회에 실패하였습니다."),

    PREMIUM_USER(false,7030,"프리미엄 계정 회원은 화분 성장치가 감소하지 않습니다."),
    MAXLEVEL_PLANT(false,7031, "성장치가 MAX 단계에 도달한 화분은 성장치가 감소하지 않습니다."),
    SAD_STATUS_PLANT(false,7032, "화분이 시무룩 상태입니다. 화분의 성장치를 증가시킬 수 없습니다."),


    // 8000 : 잭
    MODIFY_FAIL_LETTER_STATUS(false,8001,"편지 삭제 실패"),
    MODIFY_FAIL_BLOCK_STATUS(false,8002,"차단 해제 실패"),
    GET_FAIL_USERIDX(false, 8003,"해당 유저의 인덱스가 입력되지않았습니다."),
    REPLY_DATABASE_ERROR(false,8004,"답장 생성 과정에서 DB에 문제가 있습니다."),
    MODIFY_FAIL_REPLY_STATUS(false, 8005,"답장 삭제 실패"),
    MODIFY_REPLY_ISCHECKED_ERROR(false, 8007,"답장의 열람여부 변경 실패"),
    NOTICE_DATABASE_ERROR(false, 8006,"공지 조회 실패"),
    MODIFY_LETTERSENDLIST_ISCHECKED_ERROR(false,8008,"편지 열람여부 변경 실패"),
    POST_REPORT_REASON(false,8009,"신고의 사유가 정해진 사유를 벗어납니다."),
    TEMP4(false, 9000, "conflict 방지용 4");



    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) { // BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }

}
