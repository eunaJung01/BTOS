package com.umc.btos.src.notice;

import com.umc.btos.src.diary.model.PostDiaryReq;
import com.umc.btos.src.notice.model.GetNoticeRes;
import com.umc.btos.src.notice.model.PostNoticeReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class NoticeDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 공지사항 전체 반환
    public List<GetNoticeRes> getNotice() {
        // 생성 시간 Format = "년.월.일"
        String query = "SELECT noticeIdx, title, content, date_format(createdAt, '%Y.%m.%d') AS createdAt FROM Notice";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetNoticeRes(
                        rs.getInt("noticeIdx"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("createdAt")));
    }

    // 공지사항 저장 -> noticeIdx 반환
    public int postNotice(PostNoticeReq postNoticeReq) {
        String query = "INSERT INTO Notice(title, content) VALUES(?,?)";
        this.jdbcTemplate.update(query, postNoticeReq.getTitle(), postNoticeReq.getContent());

        String get_noticeIdx_query = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(get_noticeIdx_query, int.class);
    }

}
