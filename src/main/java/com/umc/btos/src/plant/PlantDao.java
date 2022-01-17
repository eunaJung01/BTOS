package com.umc.btos.src.plant;

import com.umc.btos.src.plant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

// ~ DB
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
                        "UserPlantList.status, (SELECT UserPlantList.uPlantIdx FROM UserPlantList WHERE UserPlantList.status=?) " +
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
    public GetSpecificPlantRes getSelectedPlant(int plantIdx, int userIdx) {
        String Query = "SELECT Plant.plantIdx, Plant.plantName, Plant.maxLevel, Plant.plantImgUrl, " +
                        "UserPlantList.level, UserPlantList.status, " +
                        "(SELECT UserPlantList.uPlantIdx FROM UserPlantList WHERE UserPlantList.status=?) " +
                        "FROM Plant, UserPlantList WHERE Plant.status=? AND UserPlantList.userIdx=?";
        Object[] Params = new Object[]{"selected", "active", userIdx};

        return this.jdbcTemplate.queryForObject(Query, GetSpecificPlantRes.class, Params);
    }

    //화분 선택 API
    public int selectPlant(int uPlantIdx) {
        String Query = "UPDATE UserPlantList SET status=? WHERE uPlantIdx=?";
        Object[] Params = new Object[]{"selected", uPlantIdx};

        return this.jdbcTemplate.update(Query,Params);
    }

    //화분 구매 API
    public int buyPlant(int plantIdx, int userIdx) {
        String Query = "INSERT INTO UserPlantList(userIdx, plantIdx, level, score, status, createdAt, updatedAt)" +
                "VALUES(?, ?, DEFAULT, DEFAULT, DEFAULT, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())";
        Object[] Params = new Object[]{userIdx, plantIdx};

        return this.jdbcTemplate.update(Query, Params);
    }

    //화분 보유중 목록 조회 API
    public List<GetSpecificPlantRes> getOwnPlantList(int userIdx) {
        String Query = "SELECT Plant.plantIdx, Plant.plantName, Plant.maxLevel, Plant.plantImgUrl, " +
                "UserPlantList.level, UserPlantList.status " +
                "(SELECT UserPlantList.uPlantIdx FROM UserPlantList WHERE UserPlantList.status=?) " +
                "FROM Plant, UserPlantList " +
                "WHERE UserPlantList.status = ? OR INUserPlantList.status = ? AND UserPlantList.userIdx = ?";
        Object[] Params = new Object[]{"selected", userIdx, "active", "selected"};

        return this.jdbcTemplate.query(Query,
                                (rs, rowNum) -> new GetSpecificPlantRes(
                                        rs.getInt("plantIdx"),
                                        rs.getString("plantName"),
                                        rs.getInt("maxLevel"),
                                        rs.getString("plantImgUrl"),
                                        rs.getInt("currentLevel"),
                                        rs.getString("userStatus"),
                                        rs.getInt("selectedPlantIdx"))
        );
    }

}