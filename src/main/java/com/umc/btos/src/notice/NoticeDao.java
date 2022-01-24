package com.umc.btos.src.notice;


import com.umc.btos.src.notice.model.GetNoticeRes;
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

    // Notice 테이블에 존재하는 전체 공지들 조회
    public List<GetNoticeRes> getNotices() {
        String getNoticesQuery = "select noticeIdx,title,content,date_format(createdAt, '%Y-%m-%d') from Notice"; //공지 조회 쿼리
        return this.jdbcTemplate.query(getNoticesQuery,
                (rs, rowNum) -> new GetNoticeRes(
                        rs.getInt("noticeIdx"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("date_format(createdAt, '%Y-%m-%d')"))
        );
    }
}
