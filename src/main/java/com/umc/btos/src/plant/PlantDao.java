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
                "UserPlantList.level, UserPlantList.status " +
                "FROM Plant INNER JOIN UserPlantList ON Plant.plantIdx=UserPlantList.plantIdx " +
                "WHERE UserPlantList.userIdx=?";
        Object[] Params = new Object[]{userIdx};

        return this.jdbcTemplate.query(Query, Params,
                (rs, rowNum) -> new GetPlantRes(
                        rs.getInt("Plant.plantIdx"),
                        rs.getString("Plant.plantName"),
                        rs.getString("Plant.plantImgUrl"),
                        rs.getInt("Plant.plantPrice"),
                        rs.getInt("Plant.maxLevel"),
                        rs.getInt("UserPlantList.level"),
                        rs.getString("UserPlantList.status"))
        );
    }

    //회원이 선택한 화분 조회 API
    public GetSpecificPlantRes getSelectedPlant(int plantIdx, int userIdx) {
        String Query = "SELECT Plant.plantIdx, Plant.plantName, Plant.maxLevel, Plant.plantImgUrl, " +
                "UserPlantList.level, UserPlantList.status " +
                "FROM Plant INNER JOIN UserPlantList ON Plant.plantIdx=UserPlantList.plantIdx " +
                "WHERE Plant.status=? AND UserPlantList.userIdx=?";
        Object[] Params = new Object[]{"active", userIdx};

        return this.jdbcTemplate.queryForObject(Query, GetSpecificPlantRes.class, Params);
    }

    //화분 선택 API ~ 회원이 futurePlant를 이미 selected된 화분으로 넘겼는지 체크하기 위한 함수
    public int checkPlant(int userIdx) {
        String Query = "SELECT uPlantIdx FROM UserPlantList WHERE userIdx=? AND status=?";
        Object[] Params = new Object[]{userIdx, "selected"};

        return this.jdbcTemplate.queryForObject(Query, int.class, Params);
    }

    //화분 선택 API ~ 기존에 선택되어있던 화분의 status를 active로 바꾸자 (selected -> active)
    public int activePlant(int userIdx) {
        String queryToActive = "UPDATE UserPlantList SET status=? WHERE userIdx=? AND " +
                "uPlantIdx=(SELECT Idx FROM (SELECT uPlantIdx AS Idx FROM UserPlantList WHERE status=?) T)";
        Object[] paramsToActive = new Object[]{"active", userIdx, "selected"};

        return this.jdbcTemplate.update(queryToActive, paramsToActive);
    }

    //화분 선택 API
    public int selectPlant(PatchSelectPlantReq patchSelectPlantReq) {
        //선택된 화분의 status를 selected로 바꾸자 (active -> selected)
        String Query = "UPDATE UserPlantList SET status=? WHERE userIdx=? AND uPlantIdx=?";
        Object[] Params = new Object[]{"selected", patchSelectPlantReq.getUserIdx(), patchSelectPlantReq.getFuturePlant()};

        return this.jdbcTemplate.update(Query, Params);
    }

    //화분 구매 API
    public int buyPlant(PostBuyPlantReq postBuyPlantReq) {
        String Query = "INSERT INTO UserPlantList(userIdx, plantIdx, level, score, status, createdAt, updatedAt)" +
                "VALUES(?, ?, DEFAULT, DEFAULT, DEFAULT, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())";
        Object[] Params = new Object[]{postBuyPlantReq.getUserIdx(), postBuyPlantReq.getPlantIdx()};

        return this.jdbcTemplate.update(Query, Params);
    }

    //화분의 현재 score 가져오기
    public int selectScore(int userIdx) {
        String Query = "SELECT score FROM UserPlantList WHERE userIdx = ?";
        int Param = userIdx;

        return this.jdbcTemplate.queryForObject(Query, int.class, Param);
    }

    //화분 점수 증가 (Dao)
        //score = score + addScore : 기존 점수에 addScore 더함
    public int upScore(int userIdx, int addScore) {
        String Query = "UPDATE UserPlantList SET score = score+? WHERE userIdx=?";
        Object[] Params = new Object[]{addScore, userIdx};

        return this.jdbcTemplate.update(Query, Params);
    }

    //화분 단계 변경 (Dao)
        //level = level + 1 : 기존 점수에 +1
    public int modifyLevel(int userIdx) {
        String Query = "UPDATE UserPlantList SET level = level+? WHERE userIdx=?";
        Object[] Params = new Object[]{1, userIdx};

        return this.jdbcTemplate.update(Query, Params);
    }

    /*
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
    */
}