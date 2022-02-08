package com.umc.btos.src.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
