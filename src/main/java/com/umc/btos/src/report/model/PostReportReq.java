package com.umc.btos.src.report.model;

import lombok.*;
import org.w3c.dom.Text;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReportReq {
    private int reportIdx;
    private String reportType;
    private String reason;
    private int idx;
    private String content;

}
