package com.umc.btos.src.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public TestController() {
    }

    /**
     * 로그 테스트 API
     * [GET] /test/log
     *
     * @return String
     */
    @ResponseBody
    @GetMapping("/log")
    public String getAll() {

        System.out.println("테스트");
        logger.info("INFO Level 테스트");
        logger.warn("Warn Level 테스트");
        logger.error("ERROR Level 테스트");

        return "Success Test";
    }

}
