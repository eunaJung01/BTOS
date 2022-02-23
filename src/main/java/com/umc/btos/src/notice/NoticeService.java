package com.umc.btos.src.notice;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.alarm.AlarmService;
import com.umc.btos.src.notice.model.PostNoticeReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class NoticeService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NoticeDao noticeDao;
    private final AlarmService alarmService;

    @Autowired
    public NoticeService(NoticeDao noticeDao, AlarmService alarmService) {
        this.noticeDao = noticeDao;
        this.alarmService = alarmService;
    }

    /*
     * 공지사항 저장 및 알림 발송
     * [POST] / notices
     */
    public int postNotice(PostNoticeReq postNoticeReq) throws BaseException {
        try {
            // 공지사항 저장
            int noticeIdx = noticeDao.postNotice(postNoticeReq);

            // 알림 저장
            alarmService.postAlarm_notice(noticeIdx, postNoticeReq.getTitle());

            return noticeIdx;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
