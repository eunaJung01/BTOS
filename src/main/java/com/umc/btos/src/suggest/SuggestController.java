//package com.umc.btos.src.suggest;
//
//import com.umc.btos.config.BaseException;
//import com.umc.btos.config.BaseResponse;
//import com.umc.btos.src.suggest.model.PostSuggestReq;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import static com.umc.btos.config.BaseResponseStatus.*;
//
//// SQL
///*
//create table Suggest
//        (
//        suggestIdx int auto_increment
//        primary key,
//        userIdx    int                                   not null comment '회원 식별자',
//        type       varchar(10)                           not null comment '건의 구분 (bug : 버그 제보 / user : 악성 유저 신고 / add : 추가되었으면 하는 기능 / etc : 기타)',
//        content    varchar(200)                          not null comment '내용',
//        status     varchar(10) default 'active'          not null comment '상태값 (active / deleted)',
//        createdAt  timestamp   default CURRENT_TIMESTAMP not null comment '생성일 (건의 작성일)',
//        updatedAt  timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일'
//        )
//        comment '건의';
//*/
//
//@RestController
//@RequestMapping("/suggests")
//public class SuggestController {
//    final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    @Autowired
//    private final SuggestProvider suggestProvider;
//    @Autowired
//    private final SuggestService suggestService;
//
//    public SuggestController(SuggestProvider suggestProvider, SuggestService suggestService) {
//        this.suggestProvider = suggestProvider;
//        this.suggestService = suggestService;
//    }
//
//    /*
//     * 건의 저장
//     * [POST] /suggests
//     */
//    @ResponseBody
//    @PostMapping("")
//    public BaseResponse<String> postSuggest(@RequestBody PostSuggestReq postSuggestReq) {
//        try {
//            int suggestIdx = suggestService.postSuggest(postSuggestReq);
//
//            String result = "건의 저장 완료 (suggestIdx = " + suggestIdx + ")";
//            return new BaseResponse<>(result);
//
//        } catch (BaseException exception) {
//            return new BaseResponse<>(exception.getStatus());
//        }
//    }
//
//}
