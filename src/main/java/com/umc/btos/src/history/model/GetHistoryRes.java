package com.umc.btos.src.history.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetHistoryRes<T> {
    private FirstHistory firstHistory;
    private List<Reply> replyList;
}
