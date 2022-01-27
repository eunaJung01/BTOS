package com.umc.btos.src.diary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetDoneRes {
    private int doneIdx;
    private String content;
}
