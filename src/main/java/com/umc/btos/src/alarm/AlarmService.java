package com.umc.btos.src.alarm;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.model.GetSendListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class AlarmService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AlarmDao alarmDao;

    @Autowired
    public AlarmService(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    /*
     * type = diary
     * 18:59:55 일기 발송 리스트 생성 -> 알림 테이블에 저장
     * 19:00:00 전에 알림 목록 조회 시 isSend = 0인 일기들은 조회되면 안됨
     * -> 알림 목록 조회 시 type = diary인 알림에는 조건 추가
     */
    public void postAlarm_diary(List<GetSendListRes> diarySendList) throws BaseException {
        try {
            for (GetSendListRes diary : diarySendList) {
                for (Integer receiverIdx : diary.getReceiverIdxList()) {
                    String content = "'" + diary.getSenderNickName() + "'에게서 일기가 도착했습니다.";

                    if (alarmDao.postAlarm_diary(receiverIdx, diary.getDiaryIdx(), content) == 0) {
                        throw new BaseException(POST_FAIL_ALARM);
                    }
                }
            }

        } catch (BaseException exception) {
            throw new BaseException(POST_FAIL_ALARM); // 알림 저장에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    // type = letter


    // type = reply


    // type = plant


    // type = report


    // type = notice


}
