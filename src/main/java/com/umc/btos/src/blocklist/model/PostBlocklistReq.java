package com.umc.btos.src.blocklist.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostBlocklistReq {
    private int blockIdx;
    private int userIdx; // 차단을 하는 유저의 userIdx
    private int blockedUserIdx; // 차단을 당하는 유저의 userIdx
}
