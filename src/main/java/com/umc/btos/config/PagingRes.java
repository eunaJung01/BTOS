package com.umc.btos.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagingRes {
    private boolean hasNext;
    private int currentPage;
    private int startPage = 1;
    private int endPage;
    private int dataNumPerPage; // 한 페이지에 최대로 출력되는 데이터 개수
    private int dataNum_currentPage; // 현재 페이지의 데이터 개수
    private int dataNum_total; // 총 데이터 개수

    public PagingRes(int currentPage, int dataNumPerPage) {
        this.currentPage = currentPage;
        this.dataNumPerPage = dataNumPerPage;
    }

}
