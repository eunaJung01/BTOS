package com.umc.btos.src.suggest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suggests")
public class SuggestController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final SuggestProvider suggestProvider;
    @Autowired
    private final SuggestService suggestService;

    public SuggestController(SuggestProvider suggestProvider, SuggestService suggestService) {
        this.suggestProvider = suggestProvider;
        this.suggestService = suggestService;
    }

}
