package com.umc.btos.src.suggest;

import com.umc.btos.src.suggest.model.PostSuggestReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class SuggestDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDateSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 건의 저장
    public int postSuggest(PostSuggestReq postSuggestReq) {
        String query = "INSERT INTO Suggest (userIdx, type, content) VALUES(?,?,?)";
        Object[] object = new Object[]{postSuggestReq.getUserIdx(), postSuggestReq.getType(), postSuggestReq.getContent()};
        this.jdbcTemplate.update(query, object);

        String get_suggestIdx_query = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(get_suggestIdx_query, int.class);
    }

}
