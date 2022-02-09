package com.umc.btos.src.alarm;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.alarm.model.GetAlarmRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class AlarmProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AlarmDao alarmDao;

    @Autowired
    public AlarmProvider(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    /*
     * 알림 목록 조회
     * [GET] /alarms?userIdx=
     * Alarm.status = 'active'인 알림들만 조회
     * 수신일(createdAt) 기준 내림차순 정렬
     */
    public List<GetAlarmRes> getAlarmList(int userIdx) throws BaseException, NullPointerException {
        try {
            List<GetAlarmRes> alarmList;

            // 알림 목록에 띄워줄 알림이 존재하는지 확인 (Alarm.status = 'active')
            if (alarmDao.checkAlarmList(userIdx) == 1) {
                alarmList = new ArrayList<>(alarmDao.getAlarmList(userIdx));

            } else {
                throw new NullPointerException(); // 해당 회원에게 띄워줄 알림이 없습니다.
            }

            return alarmList;

        } catch (NullPointerException exception) {
            throw new BaseException(NO_ALARM); // 해당 회원에게 띄워줄 알림이 없습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
