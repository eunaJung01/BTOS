package com.umc.btos.src.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @GetMapping("test")
    public String TestController(){
        return "Success Test";
    }

    /**
     * GitHub Actions 테스트 API
     * [GET] /test/github-actions
     *
     * @return String
     */
    @ResponseBody
    @GetMapping("/github-actions")
    public String getActions() {

        System.out.println("테스트");
        logger.info("INFO Level 테스트");
        logger.warn("Warn Level 테스트");
        logger.error("ERROR Level 테스트");

        return "Success GitHub Actions Test";
    }

    /**
     * GitHub Actions 테스트 API
     * [GET] /test/github-actions
     *
     * @return String
     */
    @ResponseBody
    @GetMapping("/github-actions")
    public String getActions() {

        System.out.println("테스트");
        logger.info("INFO Level 테스트");
        logger.warn("Warn Level 테스트");
        logger.error("ERROR Level 테스트");

        return "Success GitHub Actions Test";
    }

}
