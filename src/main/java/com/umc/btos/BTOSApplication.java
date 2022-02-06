package com.umc.btos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BTOSApplication {
    public static void main(String[] args) {
        
        @PostConstruct
        public void started() {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        }

        SpringApplication.run(BTOSApplication.class, args);

        // 메모리 사용량 출력
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("HEAP Size(M) : " + heapSize / (1024 * 1024) + " MB");
    }

}
