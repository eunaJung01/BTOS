package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetHistoryListRes<T> {
    private String filtering;
    private List<T> list; // filtering = 1. sender -> List<HistoryList_Sender> / 2. diary, letter -> List<History>

    public GetHistoryListRes(String filtering) {
        this.filtering = filtering;
    }

}
