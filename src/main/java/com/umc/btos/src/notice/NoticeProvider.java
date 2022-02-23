package com.umc.btos.src.notice;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.notice.model.GetNoticeRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.NOTICE_DATABASE_ERROR;

@Service
public class NoticeProvider {

    private final NoticeDao noticeDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public NoticeProvider(NoticeDao noticeDao) {
        this.noticeDao = noticeDao;
    }

    /*
     * 공지사항 전체 조회
     * [GET] /notices
     */
    public List<GetNoticeRes> getNotice() throws BaseException {
        try {
            return noticeDao.getNotice();

        } catch (Exception exception) {
            throw new BaseException(NOTICE_DATABASE_ERROR);
        }
    }

}
