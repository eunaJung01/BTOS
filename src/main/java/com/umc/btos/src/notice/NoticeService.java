package com.umc.btos.src.notice;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.alarm.AlarmService;
import com.umc.btos.src.notice.model.PostNoticeReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class NoticeService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NoticeDao noticeDao;
    private final AlarmService alarmService;
    private final NoticeFcmService fcmService;

    @Autowired
    public NoticeService(NoticeDao noticeDao, AlarmService alarmService, NoticeFcmService fcmService) {
        this.noticeDao = noticeDao;
        this.alarmService = alarmService;
        this.fcmService = fcmService;
    }

    /*
     * 공지사항 저장 및 알림 발송
     * [POST] /notices
     */
    public int postNotice(PostNoticeReq postNoticeReq) throws BaseException {
        try {
            // 공지사항 저장
            int noticeIdx = noticeDao.postNotice(postNoticeReq);

            // 알림 저장
            alarmService.postAlarm_notice(noticeIdx, postNoticeReq.getTitle());

            // 푸시 알림 수신 동의한 유저들에게 푸시 알림 발송
            // 푸시 알림 수신한 유저의 인덱스와 디바이스 토큰 리스트 형태로 반환 후 반복문으로 알림 요청
            ArrayList<String> pushAlarmToUsers =
                    noticeDao.pushNotices();

            // 푸시 알림 전송
            for (String token : pushAlarmToUsers) {
                fcmService.sendMessageTo(token, postNoticeReq.getTitle(), postNoticeReq.getContent());
            }

            return noticeIdx;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
