package com.umc.btos.src.alarm;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.alarm.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            List<GetAlarmListRes> alarmList = alarmProvider.getAlarmList(userIdx);
            return new BaseResponse<>(alarmList);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 알림 조회
     * [GET] /alarms/:alarmIdx?userIdx=&type=&typeIdx=
     */
    @ResponseBody
    @GetMapping("/{alarmIdx}")
    BaseResponse<GetAlarmRes> getAlarm(@PathVariable("alarmIdx") int alarmIdx, @RequestParam("userIdx") int userIdx, @RequestParam("type") String type, @RequestParam("typeIdx") int typeIdx) {
        try {
            GetAlarmRes alarm = alarmProvider.getAlarm(alarmIdx, userIdx, type, typeIdx);
            return new BaseResponse<>(alarm);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
