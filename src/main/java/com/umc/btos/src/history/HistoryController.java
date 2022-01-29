package com.umc.btos.src.history;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.history.model.GetHistoryListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * History 목록 조회
     * [GET] /histories/list/:userIdx/:pageNum?filtering=&search=
     * filtering = 1. sender : 발신인 / 2. diary : 일기만 / 3. letter : 편지만
     * search = 검색할 문자열 ("String")
     * 최신순 정렬 (createdAt 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    @ResponseBody
//    @GetMapping("/list/{userIdx}/{pageNum}")
    @GetMapping("/list/{userIdx}")
    BaseResponse<GetHistoryListRes> getHistoryList(@PathVariable("userIdx") String userIdx, @RequestParam(value = "filtering", defaultValue = "sender") String filtering, @RequestParam(value = "search", required = false) String search) {
        try {
            String[] params = new String[]{userIdx, filtering, search};
            GetHistoryListRes historyList = historyProvider.getHistoryList(params);
            return new BaseResponse<>(historyList);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
