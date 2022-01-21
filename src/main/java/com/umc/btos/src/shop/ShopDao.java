package com.umc.btos.src.shop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class ShopDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    //프리미엄 계정으로 변경 API
    public int joinPremium(int userIdx) {
        String Query = "UPDATE User SET isPremium = ? WHERE userIdx =?";
        Object[] Params = new Object[]{"premium", userIdx};

        return this.jdbcTemplate.update(Query, Params);
    }

    //청약철회 API
    public int withdrawPremium(int userIdx) {
        String Query = "UPDATE User SET isPremium = ? WHERE userIdx = ?";
        Object[] Params = new Object[]{"free", userIdx};

        return this.jdbcTemplate.update(Query, Params);
    }
}
