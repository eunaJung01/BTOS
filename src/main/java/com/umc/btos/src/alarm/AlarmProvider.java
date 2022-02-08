package com.umc.btos.src.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlarmProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AlarmDao alarmDao;

    @Autowired
    public AlarmProvider(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

}
