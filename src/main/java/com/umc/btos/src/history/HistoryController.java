package com.umc.btos.src.history;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.*;
import com.umc.btos.src.history.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

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
            // TODO : 형식적 validation - 존재하는 회원인가? / pageNum == 0인 경우
            if (historyProvider.checkUserIdx(Integer.parseInt(userIdx)) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (pageNum == 0) {
                throw new BaseException(PAGENUM_ERROR_0); // 페이지 번호는 1부터 시작합니다.
            }

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
    BaseResponsePaging<List<History>> getHistoryList_sender(@PathVariable("userIdx") String userIdx, @PathVariable("senderNickName") String senderNickName, @PathVariable("pageNum") int pageNum, @RequestParam(value = "search", required = false) String search) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? / 존재하는 회원 닉네임인가? / pageNum == 0인 경우
            if (historyProvider.checkUserIdx(Integer.parseInt(userIdx)) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (historyProvider.checkNickName(senderNickName) == 0) {
                throw new BaseException(INVALID_NICKNAME); // 존재하지 않는 회원 닉네임입니다.
            }
            if (pageNum == 0) {
                throw new BaseException(PAGENUM_ERROR_0); // 페이지 번호는 1부터 시작합니다.
            }

            String[] params = new String[]{userIdx, senderNickName, search};
            PagingRes pageInfo = new PagingRes(pageNum, Constant.HISTORY_DATA_NUM); // 페이징 정보

            List<History> historyList_sender = historyProvider.getHistoryList_sender(params, pageInfo);
            return new BaseResponsePaging<>(historyList_sender, pageInfo);

        } catch (BaseException exception) {
            return new BaseResponsePaging<>(exception.getStatus());
        }
    }

    /*
     * History 본문 보기 (일기 or 편지 & 답장 리스트)
     * [GET] /histories/:userIdx/:type/:typeIdx
     * type = 조회하고자 하는 본문의 type (일기일 경우 diary, 편지일 경우 letter, 답장일 경우 reply)
     * typeIdx = 조회하고자 하는 본문의 식별자 (diary - diaryIdx / letter - letterIdx / reply - replyIdx)
     * createdAt 기준 오름차순 정렬
     */
    @ResponseBody
    @GetMapping("/{userIdx}/{type}/{typeIdx}")
    BaseResponse<List<GetHistoryRes_Main>> getHistory_main(@PathVariable("userIdx") int userIdx, @PathVariable("type") String type, @PathVariable("typeIdx") int typeIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? / type(diary, letter, reply) 입력 확인 / 해당 type에 존재하는 typeIdx인가?
            if (historyProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (type.compareTo("diary") == 0 && type.compareTo("letter") == 0 && type.compareTo("reply") == 0) {
                throw new BaseException(INVALID_TYPE); // 잘못된 type 입니다. (diary, letter, reply 중 1)
            }
            if (historyProvider.checkTypeIdx(type, typeIdx) == 0) {
                throw new BaseException(INVALID_TYPEIDX_ABOUT_TYPE); // 해당 type에 존재하지 않는 typeIdx 입니다.
            }

            List<GetHistoryRes_Main> history = historyProvider.getHistory_main(userIdx, type, typeIdx);
            return new BaseResponse<>(history);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
