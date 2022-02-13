package com.umc.btos.src.blocklist;


import com.umc.btos.src.blocklist.model.*;

import com.umc.btos.src.letter.model.GetLetterRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class BlocklistDao {
    private JdbcTemplate jdbcTemplate;
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 차단 생성  // 즉 DB의 BlockList Table에 (userIdx,blockedUserIdx)값을 가지는 차단 데이터를 삽입(생성)한다.
    public int createBlocklist(PostBlocklistReq postBlocklistReq) {
        String createBlocklistQuery = "insert into BlockList (userIdx,blockedUserIdx) VALUES (?,?)";
        Object[] createBlocklistParams = new Object[]{postBlocklistReq.getUserIdx(),postBlocklistReq.getBlockedUserIdx()};
        this.jdbcTemplate.update(createBlocklistQuery, createBlocklistParams);

        String lastInsertIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery, int.class); // 해당 쿼리문의 결과 마지막으로 삽인된 차단의 blockIdx번호를 반환한다.
    }


    //차단 제거 // 차단의 status를 deleted로 변경
    public int modifyBlockStatus(PatchBlocklistReq patchBlocklistReq) {
        String modifyBlockStatusQuery = "update BlockList set status = ? where blockIdx = ? "; // 해당 blockIdx를 만족하는 block의 status를 deleted으로 변경한다.
        Object[] modifyBlockStatusParams = new Object[]{"deleted", patchBlocklistReq.getBlockIdx()}; // 주입될 값들(status, blockIdx) 순

        return this.jdbcTemplate.update(modifyBlockStatusQuery, modifyBlockStatusParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
    }


    //모든 차단 조회 API // status가 active인 차단 모두를 list형태로 반환
    public List<GetBlocklistRes> getBlockListNickName(int userIdx) {
        String getBlockQuery = "select nickName,blockIdx,blockedUserIdx from BlockList B, User U where B.userIdx=? and B.status='active' and U.userIdx = blockedUserIdx";

        return this.jdbcTemplate.query(getBlockQuery,
                (rs, rowNum) -> new GetBlocklistRes(
                        rs.getString("nickName"),
                        rs.getInt("blockIdx"),
                        rs.getInt("blockedUserIdx")),
                userIdx);
    }

}
