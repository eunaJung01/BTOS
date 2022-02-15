package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// History 목록 조회
public class GetHistoryListRes<T> {
    private List<T> list; // filtering = 1. sender -> List<History_Sender> / 2. diary, letter -> List<History>
}
