package com.umc.btos.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PagingRes {
    private boolean hasNext;
    private int startPage;
    private int endPage;
    private int currentPage;
    private int dataPerPage;
}
