package com.umc.btos.src.notice;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.blocklist.BlocklistDao;
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

    // 공지들 조회
    public List<GetNoticeRes> getNotices() throws BaseException {
        try {
            List<GetNoticeRes> getNoticeRes = noticeDao.getNotices();
            return getNoticeRes;
        } catch (Exception exception) { // 에러가 발생하였다면 : 8006 : 공지 조회 실패
            throw new BaseException(NOTICE_DATABASE_ERROR);
        }
    }
}
