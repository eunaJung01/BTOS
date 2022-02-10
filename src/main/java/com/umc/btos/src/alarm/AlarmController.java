package com.umc.btos.src.alarm;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.alarm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/alarms")
public class AlarmController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final AlarmProvider alarmProvider;
    @Autowired
    private final AlarmService alarmService;

    public AlarmController(AlarmProvider alarmProvider, AlarmService alarmService) {
        this.alarmProvider = alarmProvider;
        this.alarmService = alarmService;
    }

    /*
     * 알림 목록 조회
     * [GET] /alarms?userIdx=
     * Alarm.status = 'active'인 알림들만 조회
     * 수신일(createdAt) 기준 내림차순 정렬
     */
    @ResponseBody
    @GetMapping("")
    BaseResponse<List<GetAlarmListRes>> getAlarmList(@RequestParam("userIdx") int userIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가?
            if (alarmProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }

            List<GetAlarmListRes> alarmList = alarmProvider.getAlarmList(userIdx);
            return new BaseResponse<>(alarmList);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 알림 조회
     * [GET] /alarms/:alarmIdx?userIdx
     */
    @ResponseBody
    @GetMapping("/{alarmIdx}")
    BaseResponse<GetAlarmRes> getAlarm(@PathVariable("alarmIdx") int alarmIdx, @RequestParam("userIdx") int userIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? / 해당 회원의 알림인가? / status = active
            if (alarmProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않는 회원입니다.
            }
            if (alarmProvider.checkUserAboutAlarm(alarmIdx, userIdx) == 0) {
                throw new BaseException(INVALID_USER_ABOUT_ALARM); // 해당 알림에 접근 권한이 없는 회원입니다.
            }
            if (alarmProvider.checkStatus_active(alarmIdx) == 0) {
                throw new BaseException(INACTVIE_ALARM); // 이미 확인된 알림입니다.
            }

            GetAlarmRes alarm = alarmProvider.getAlarm(alarmIdx, userIdx);
            return new BaseResponse<>(alarm);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
