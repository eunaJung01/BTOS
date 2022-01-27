package com.umc.btos.src.history;

import com.umc.btos.config.BaseResponsePaging;
import com.umc.btos.src.history.model.GetHistoryListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/histories")
public class HistoryController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final HistoryProvider historyProvider;

    public HistoryController(HistoryProvider historyProvider) {
        this.historyProvider = historyProvider;
    }

    /*
     * History 조회
     * [GET] /histories/list/:userIdx/:pageNum?filtering=&search=
     * filtering = 1. receiver : 발신인 / 2. diary : 일기만 / 3. letter : 편지만
     * search = 검색할 문자열 ("String")
     * 최신순 정렬 (1. receiver : diaryDate & createdAt 기준 / 2. diary : diaryDate 기준 / 3. letter : createdAt 기준 -> 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
//    @ResponseBody
//    @GetMapping("/list/{userIdx}/{pageNum}")
//    BaseResponsePaging<List<GetHistoryListRes>> getHistoryList() {
//        try {
//
//        }
//    }

}
