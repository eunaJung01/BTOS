package com.umc.btos.src.notice;


import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.notice.model.GetNoticeRes;
import com.umc.btos.src.notice.model.PostNoticeReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/notices")
public class NoticeController {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기
    @Autowired
    private final NoticeProvider noticeProvider;
    @Autowired
    private final NoticeService noticeService;


    public NoticeController(NoticeProvider noticeProvider, NoticeService noticeService) {
        this.noticeProvider = noticeProvider;
        this.noticeService = noticeService;
    }

    /*
     * 공지사항 전체 조회
     * [GET] /notices
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetNoticeRes>> getNotice() {
        try {
            List<GetNoticeRes> getNoticeRes = noticeProvider.getNotice();
            return new BaseResponse<>(getNoticeRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /*
     * 공지사항 저장 및 알림 발송
     * [POST] / notices
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<String> postNotice(@RequestBody PostNoticeReq postNoticeReq) {
        try {
            int noticeIdx = noticeService.postNotice(postNoticeReq);

            String result = "공지사항 저장 및 알림 발송 완료 (noticeIdx = " + noticeIdx + ")";
            return new BaseResponse<>(result);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
