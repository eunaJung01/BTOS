package com.umc.btos.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor

public class GetUserRes {
    private int userIdx;
    private String email;
    private String nickName;
    private int birth;
    private String isPremium;
    private boolean recOthers;
    private boolean recSimilarAge;
    private int fontIdx;
    private boolean pushAlarm;
    private boolean isSad;
}
