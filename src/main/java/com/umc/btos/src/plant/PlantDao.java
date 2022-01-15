package com.umc.btos.src.plant;

import com.umc.btos.src.plant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PlantDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    //모든식물조회(상점) API
    public List<GetPlantRes> getAllPlant(int userIdx) {
        String Query = "SELECT Plant.plantIdx, Plant.plantName, Plant.plantImgUrl, Plant.plantPrice, Plant.maxLevel, " +
                        "UserPlantList.status, (SELECT UserPlantList.plantIdx FROM UserPlantList WHERE UserPlantList.status=?) " +
                        "FROM Plant, UserPlantList WHERE UserPlantList.userIdx=? AND Plant.status=?";
        Object[] Params = new Object[]{"selected", userIdx, "active"};
        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> new GetPlantRes(
                        rs.getInt("plantIdx"),
                        rs.getString("plantName"),
                        rs.getString("plantImgUrl"),
                        rs.getInt("plantPrice"),
                        rs.getInt("maxLevel"),
                        rs.getString("userStatus"),
                        rs.getInt("selectedPlantIdx"))
        );
    }

    //회원이 선택한 화분 조회 API
    public GetPlantRes getSelectedPlant(int plantIdx, int userIdx) {
        String Query = "SELECT Plant.plantIdx, Plant.plantName, Plant.maxLevel, Plant.plantImgUrl, " +
                        "UserPlantList.level, UserPlantList.status, " +
                        "(SELECT UserPlantList.selectedPlantIdx FROM UserPlantList WHERE UserPlantList.status=? " +
                        "FROM Plant, UserPlantList WHERE Plant.status=? AND UserPlantList.userIdx=?";
        Object[] Params = new Object[]{"selected", "active", userIdx};

        return this.jdbcTemplate.queryForObject(Query, GetPlantRes.class, Params);
    }

    //화분 선택 API
    public int selectPlant(int uPlantIdx) {
        String Query = "UPDATE UserPlantList SET status=? WHERE uPlantIdx=?";
        Object[] Params = new Object[]{"selected", uPlantIdx};

        return this.jdbcTemplate.update(Query,Params);
    }

}