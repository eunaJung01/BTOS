package com.umc.btos.src.blocklist.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetBlocklistRes {
    private String blockedNickname; // 신고당한 유저의 닉네임
    private int blockIdx;
    private int blockedUserIdx; // 신고당한 유저의 userIdx
}
