package com.umc.btos.src.blocklist.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostBlocklistReq {
    private int blockIdx;
    private int userIdx;
    private int blockedUserIdx;
}
