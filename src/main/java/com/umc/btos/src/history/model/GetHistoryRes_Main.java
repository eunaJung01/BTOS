package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// History 본문 보기
public class GetHistoryRes_Main<T> {
    private List<History_Main> history;
}
