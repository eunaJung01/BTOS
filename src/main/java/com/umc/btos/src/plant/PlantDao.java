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

    //회원에게 plantIdx 화분이 존재하는지 확인
    //결과값이 존재하면 1, 없으면 0
    public int checkPlantExist(int plantIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserPlantList WHERE plantIdx=?) as success";
        int Param = plantIdx;

        return this.jdbcTemplate.queryForObject(Query, int.class, Param);
    }

    //회원이 선택한 화분 조회 API
    public GetSpecificPlantRes getSelectedPlant(int plantIdx, int status, int userIdx) {
        //화분 기본 정보 FROM Plant
        String plantQuery = "SELECT plantIdx, plantName, plantImgUrl, plantInfo, plantPrice, maxLevel " +
                "FROM Plant WHERE plantIdx=? AND status=?";
        Object[] plantParams = new Object[]{plantIdx, "active"};

        PlantBasicInfo plantBasicInfo = jdbcTemplate.queryForObject(
                plantQuery,
                (rs, rowNum) -> new PlantBasicInfo(
                        rs.getInt("plantIdx"),
                        rs.getString("plantName"),
                        rs.getString("plantImgUrl"),
                        rs.getString("plantInfo"),
                        rs.getInt("plantPrice"),
                        rs.getInt("maxLevel")),
                plantParams);

        int level = 0; // 현재 레벨 변수, 보유중이면 현재레벨/미보유면 -1
        if (status == 1) { //보유중이면 화분 현재 레벨 가져오기
            String currentLevelQuery = "SELECT level FROM UserPlantList WHERE userIdx=? AND plantIdx=?";
            Object[] currentLevelParams = new Object[]{userIdx, plantIdx};

            level = jdbcTemplate.queryForObject(currentLevelQuery, int.class, currentLevelParams);
        } else { //미보유
            level = -1;
        }

        //반환 형태
        GetSpecificPlantRes getSpecificPlantRes = new GetSpecificPlantRes(
                plantBasicInfo.getPlantIdx(), plantBasicInfo.getPlantName(), plantBasicInfo.getPlantImgUrl(),
                plantBasicInfo.getPlantInfo(), plantBasicInfo.getPlantPrice(), plantBasicInfo.getMaxLevel(),
                level, status);

        return getSpecificPlantRes;
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
        String Query = "SELECT score FROM UserPlantList WHERE userIdx = ? AND status=?";
        Object[] Params = new Object[]{userIdx, "selected"};

        return this.jdbcTemplate.queryForObject(Query, int.class, Params);
    }

    //화분의 현재 level 가져오기
    public int selectLevel(int userIdx) {
        String Query = "SELECT level FROM UserPlantList WHERE userIdx =? AND status=?";
        Object[] Params = new Object[]{userIdx, "selected"};

        return this.jdbcTemplate.queryForObject(Query, int.class, Params);
    }

    //userIdx의 selected 화분의 MAX Level 가져오기
    public int maxLevel(int userIdx) {

        //userIdx의 selected 화분의 plantIdx 가져오기
        String plantIdxQuery = "SELECT plantIdx FROM UserPlantList " +
                "WHERE userIdx=? AND status=?";
        Object[] plantIdxParams = new Object[]{userIdx, "selected"};
        int selectedPlantIdx = this.jdbcTemplate.queryForObject(plantIdxQuery, int.class, plantIdxParams);

        //selectedPlantIdx이용해서 해당 화분의 maxLevel 가져오기
        String maxLevelQuery = "SELECT maxLevel FROM Plant WHERE plantIdx=?";
        int maxLevelParam = selectedPlantIdx;

        return this.jdbcTemplate.queryForObject(maxLevelQuery, int.class, maxLevelParam);
    }

    /*
    //화분 점수 더함 (Dao) : 증가, 감소에 쓰임
    //score = score + addScore : 기존 점수에 addScore(양수/음수) 더함
    public int plusScore(int userIdx, int addScore) {
        String Query = "UPDATE UserPlantList SET score = score+? WHERE userIdx=? AND status=?";
        Object[] Params = new Object[]{addScore, userIdx, "selected"};

        return this.jdbcTemplate.update(Query, Params);
    }


    //화분 점수를 addRes 숫자로 업데이트(단계까지 감소되는 경우의 점수 감소에 사용됨, 이 함수는 plusScore와 다름)
    public int setDownScore(int userIdx, int addRes) {
        String Query = "UPDATE UserPlantList SET score = ? WHERE userIdx=? AND status=?";
        Object[] Params = new Object[]{addRes, userIdx, "selected"};

        return this.jdbcTemplate.update(Query, Params);
    }



    //화분 단계 증가 (Dao)
    //level = level + 1 : 기존 단계에 +1
    public int upLevel(int userIdx) {
        String Query = "UPDATE UserPlantList SET level = level+? WHERE userIdx=? AND status=?";
        Object[] Params = new Object[]{1, userIdx, "selected"};

        return this.jdbcTemplate.update(Query, Params);
    }



    //화분 단계 감소
    //level = level - totalDec
    public int downLevel(int userIdx, int totalDec) {
        String Query = "UPDATE UserPlantList SET level = level-? WHERE userIdx=? AND status=?";
        Object[] Params = new Object[]{totalDec, userIdx, "selected"};

        return this.jdbcTemplate.update(Query, Params);
    }
    */


    //프리미엄 계정인지 확인
    public String checkPremium(int userIdx) {
        String Query = "SELECT isPremium FROM User WHERE userIdx=?";
        int Param = userIdx;

        return this.jdbcTemplate.queryForObject(Query, String.class, Param);
    }


    //화분이 시무룩 상태인지 확인
    public boolean checkSad(int userIdx) {
        String Query = "SELECT isSad FROM User WHERE userIdx=?";
        int Param = userIdx;

        return this.jdbcTemplate.queryForObject(Query, boolean.class, Param);
    }

}