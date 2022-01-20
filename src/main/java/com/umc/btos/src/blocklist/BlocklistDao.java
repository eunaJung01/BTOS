package com.umc.btos.src.blocklist;


import com.umc.btos.src.blocklist.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class BlocklistDao {
    private JdbcTemplate jdbcTemplate;
    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int createBlocklist(PostBlocklistReq postBlocklistReq) {
        String createBlocklistQuery = "insert into BlockList (userIdx,blockedUserIdx) VALUES (?,?)"; // 실행될 동적 쿼리문
        Object[] createBlocklistParams = new Object[]{postBlocklistReq.getUserIdx(),postBlocklistReq.getBlockedUserIdx()}; // 동적 쿼리의 ?부분에 주입될 값
        this.jdbcTemplate.update(createBlocklistQuery, createBlocklistParams);

        // 즉 DB의 Report Table에 (reportType,reason,idx,content)값을 가지는 유저 데이터를 삽입(생성)한다.

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 유저의 userIdx번호를 반환한다.
    }
    // 편지 status 변경
    public int modifyBlockStatus(PatchBlocklistReq patchBlocklistReq) {
        String modifyBlockStatusQuery = "update BlockList set status = ? where blockIdx = ? "; // 해당 blockIdx를 만족하는 block의 status를 deleted으로 변경한다.
        Object[] modifyBlockStatusParams = new Object[]{"deleted", patchBlocklistReq.getBlockIdx()}; // 주입될 값들(status, blockIdx) 순

        return this.jdbcTemplate.update(modifyBlockStatusQuery, modifyBlockStatusParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }

}
