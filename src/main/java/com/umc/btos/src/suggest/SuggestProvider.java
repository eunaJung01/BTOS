package com.umc.btos.src.suggest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuggestProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SuggestDao suggestDao;

    @Autowired
    public SuggestProvider(SuggestDao suggestDao) {
        this.suggestDao = suggestDao;
    }

}
