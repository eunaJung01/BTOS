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
     * filtering = 1. sender : 발신인 / 2. diary : 일기만 / 3. letter : 편지만
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
    BaseResponsePaging<GetSenderRes> getHistoryList_sender(@PathVariable("userIdx") String userIdx, @PathVariable("senderNickName") String senderNickName, @PathVariable("pageNum") int pageNum, @RequestParam(value = "search", required = false) String search) {
        try {
            String[] params = new String[]{userIdx, senderNickName, search};
            PagingRes pageInfo = new PagingRes(pageNum, Constant.HISTORY_DATA_NUM); // 페이징 정보

            GetSenderRes historyList_sender = historyProvider.getHistoryList_sender(params, pageInfo);
            return new BaseResponsePaging<>(historyList_sender, pageInfo);

        } catch (BaseException exception) {
            return new BaseResponsePaging<>(exception.getStatus());
        }
    }

    /*
     * History 본문 보기 (일기 or 편지 & 답장 리스트)
     * [GET] /histories/:userIdx/:mainType/:mainIdx
     * mainType = 어디서부터 시작된 답장인가? 1. diary : 일기 / 2. letter : 편지
     * mainIdx = 답장 시작점(일기 또는 편지)의 식별자 (diary - diaryIdx / letter - letterIdx)
     * 최신순 정렬 (createdAt 기준 내림차순 정렬)
     */
    @ResponseBody
    @GetMapping("/{userIdx}/{mainType}/{mainIdx}")
    BaseResponse<GetHistoryRes> getHistory_main(@PathVariable("userIdx") int userIdx, @PathVariable("mainType") String mainType, @PathVariable("mainIdx") int mainIdx) {
        try {
            GetHistoryRes history = historyProvider.getHistory_main(userIdx, mainType, mainIdx);
            return new BaseResponse<>(history);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
