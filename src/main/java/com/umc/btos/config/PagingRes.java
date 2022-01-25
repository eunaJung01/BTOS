package com.umc.btos.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingRes {
    private boolean hasNext;
    private int startPage = 1;
    private int endPage;
    private int currentPage;
    private int dataPerPage;

    public PagingRes(int currentPage, int dataPerPage) {
        this.currentPage = currentPage;
        this.dataPerPage = dataPerPage;
    }

}
