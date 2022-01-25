package com.umc.btos.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingRes {
    private boolean hasNext;
    private int startPage;
    private int endPage;
    private int currentPage;
    private int dataPerPage;

    public PagingRes(int currentPage) {
        this.currentPage = currentPage;
    }

}
