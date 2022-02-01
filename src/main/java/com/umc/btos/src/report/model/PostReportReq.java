package com.umc.btos.src.report.model;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReportReq {
    private int reportIdx;
    private String reportType; // 신고구분  // diary:일기, letter : 편지, reply : 답장
    private String reason; // 신고사유 // spam : 스팸 / sex : 성적 / hate : 혐오 / dislike : 마음에 안듦 / etc : 기타
    private int idx; // 신고구분의 식별자
    private String content; // null 가능 // 기타를 선택한 경우

}
