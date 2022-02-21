package com.umc.btos.src.report.model;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class PostReportReq {
    private int reportIdx;
    private String type; // 신고 구분 (Tables - diary : 일기 / letter : 편지 / reply : 답장)
    private int typeIdx; // 신고 구분 식별자 (PKs - diaryIdx / letterIdx / replyIdx)
    private String reason; // 신고 사유 (spam : 스팸 / sex : 성적 컨텐츠 / hate : 혐오 발언 또는 괴롭힘 / dislike : 마음에 들지 않습니다 / etc : 기타)
    private String content; // 내용 (reason = etc만 작성, 나머지는 null)
}
