package com.umc.btos.src.notice;



import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.notice.model.GetNoticeRes;
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

    /**
     * 공지사항 목록 조회 API
     * [GET] /notices
     *
     *
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetNoticeRes>> getNotices() { // 공지사항 조회 함수
        try {
            //조회 성공 시 : List<GetBlocklistRes> 형태로 결과(공지사항 목록) 반환
            List<GetNoticeRes> getNoticeRes = noticeProvider.getNotices();
            return new BaseResponse<>(getNoticeRes);

        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
