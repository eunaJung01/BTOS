package com.umc.btos.src.blocklist.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetBlocklistRes {
    private String blockedNickname; // 신고당한 유저의 Idx
    private int blockIdx;
    private int blockedUserIdx;
}
