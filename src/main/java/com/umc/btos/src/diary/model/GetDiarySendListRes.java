package com.umc.btos.src.diary.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetDiarySendListRes {
    private List<DiarySendList> diary;
}
