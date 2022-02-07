package com.umc.btos.src.history.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
// History 본문 보기 (type = diary)
public class Done {
    private int doneIdx;
    private String content;
}
