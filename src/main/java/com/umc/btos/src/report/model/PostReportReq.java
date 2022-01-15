package com.umc.btos.src.report.model;

import lombok.*;
import org.w3c.dom.Text;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버를 받는 생성자를 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReportReq {
    private int reportIdx;
    private String reportType;
    private String reason;
    private int idx;
    private String content;

}
