package com.umc.btos.src.blocklist.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetBlocklistRes {
    private int blockIdx;
    private int blockedUserIdx;
}
