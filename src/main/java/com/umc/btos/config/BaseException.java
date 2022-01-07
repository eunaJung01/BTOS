package com.umc.btos.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BaseException extends Exception {
    private com.umc.btos.config.BaseResponseStatus status;
}
