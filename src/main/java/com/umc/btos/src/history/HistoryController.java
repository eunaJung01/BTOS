package com.umc.btos.src.history;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.*;
import com.umc.btos.src.history.model.*;
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
     * filtering = 1. sender : 발신인 (Diary, Letter, Reply) / 2. diary : 일기만 (Diary) / 3. letter : 편지만 (Letter, Reply)
     * search = 검색할 문자열 ("String")
     * 최신순 정렬 (createdAt 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    @ResponseBody
    @GetMapping("/list/{userIdx}/{pageNum}")
    BaseResponsePaging<GetHistoryListRes> getHistoryList(@PathVariable("userIdx") String userIdx, @PathVariable("pageNum") int pageNum, @RequestParam(value = "filtering", defaultValue = "sender") String filtering, @RequestParam(value = "search", required = false) String search) {
        try {
            String[] params = new String[]{userIdx, filtering, search};
            PagingRes pageInfo = new PagingRes(pageNum, Constant.HISTORY_DATA_NUM); // 페이징 정보

            GetHistoryListRes historyList = historyProvider.getHistoryList(params, pageInfo);
            return new BaseResponsePaging<>(historyList, pageInfo);

        } catch (BaseException exception) {
            return new BaseResponsePaging<>(exception.getStatus());
        }
    }

    /*
     * History 발신인 조회
     * [GET] /histories/sender/:userIdx/:senderNickName/:pageNum?search=
     * search = 검색할 문자열 ("String")
     * 최신순 정렬 (createdAt 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    @ResponseBody
    @GetMapping("/sender/{userIdx}/{senderNickName}/{pageNum}")
    BaseResponsePaging<GetHistoryRes_Sender> getHistoryList_sender(@PathVariable("userIdx") String userIdx, @PathVariable("senderNickName") String senderNickName, @PathVariable("pageNum") int pageNum, @RequestParam(value = "search", required = false) String search) {
        try {
            String[] params = new String[]{userIdx, senderNickName, search};
            PagingRes pageInfo = new PagingRes(pageNum, Constant.HISTORY_DATA_NUM); // 페이징 정보

            GetHistoryRes_Sender historyList_sender = historyProvider.getHistoryList_sender(params, pageInfo);
            return new BaseResponsePaging<>(historyList_sender, pageInfo);

        } catch (BaseException exception) {
            return new BaseResponsePaging<>(exception.getStatus());
        }
    }

    /*
     * History 본문 보기 (일기 or 편지 & 답장 리스트)
     * [GET] /histories/:userIdx/:type/:idx
     * type = 조회하고자 하는 본문의 type (일기일 경우 diary, 편지일 경우 letter, 답장일 경우 reply)
     * idx = 조회하고자 하는 본문의 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
     * createdAt 기준 오름차순 정렬
     */
    @ResponseBody
    @GetMapping("/{userIdx}/{type}/{idx}")
    BaseResponse<GetHistoryRes_Main> getHistory_main(@PathVariable("userIdx") int userIdx, @PathVariable("type") String type, @PathVariable("idx") int idx) {
        try {
            GetHistoryRes_Main history = historyProvider.getHistory_main(userIdx, type, idx);
            return new BaseResponse<>(history);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
