package com.umc.btos.src.plant;

import com.umc.btos.src.plant.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

// ~ DB
@Repository
public class PlantDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    //회원에게 plantIdx 화분이 존재하는지 확인
    //결과값이 존재하면 1, 없으면 0
    public int checkPlantExist(int plantIdx, int userIdx) {
        String Query = "SELECT EXISTS(SELECT * FROM UserPlantList WHERE plantIdx=? AND userIdx=?) as success";
        Object[] Params = new Object[] {plantIdx, userIdx};

        return this.jdbcTemplate.queryForObject(Query, int.class, Params);
    }

    //회원이 선택한 화분 조회 API
    public GetPlantRes getSelectedPlant(int plantIdx, int status, int userIdx) {

        GetPlantRes getPlantRes = new GetPlantRes();

        //화분 기본 정보 FROM Plant
        String plantQuery = "SELECT plantIdx, plantName, plantInfo, plantPrice, maxLevel " +
                "FROM Plant WHERE plantIdx=? AND status=?";
        Object[] plantParams = new Object[]{plantIdx, "active"};

        PlantBasicInfo plantBasicInfo = jdbcTemplate.queryForObject(
                plantQuery,
                (rs, rowNum) -> new PlantBasicInfo(
                        rs.getInt("plantIdx"),
                        rs.getString("plantName"),
                        rs.getString("plantInfo"),
                        rs.getInt("plantPrice"),
                        rs.getInt("maxLevel")),
                plantParams);

        getPlantRes.setPlantIdx(plantBasicInfo.getPlantIdx());
        getPlantRes.setPlantName(plantBasicInfo.getPlantName());
        getPlantRes.setPlantInfo(plantBasicInfo.getPlantInfo());
        getPlantRes.setMaxLevel(plantBasicInfo.getMaxLevel());

        if (status == 1) { //보유중이면 화분 현재 레벨, 보유 상태 가져오기
            String currentLevelQuery = "SELECT level FROM UserPlantList WHERE userIdx=? AND plantIdx=?";
            Object[] Params = new Object[]{userIdx, plantIdx};
            int level = jdbcTemplate.queryForObject(currentLevelQuery, int.class, Params);

            String statusQuery = "SELECT status FROM UserPlantList WHERE userIdx=? AND plantIdx=?";
            String plantStatus = jdbcTemplate.queryForObject(statusQuery, String.class, Params);

            getPlantRes.setCurrentLevel(level); // 현재레벨
            getPlantRes.setPlantStatus(plantStatus); // "active" OR "selected"
        } else { //미보유
            getPlantRes.setPlantStatus("inactive");
            getPlantRes.setIsOwn(false);
        }

        return getPlantRes;
    }

    //화분 선택 API ~ 회원이 futurePlant를 이미 selected된 화분으로 넘겼는지 체크하기 위한 함수
    public int checkPlant(int userIdx) {
        String Query = "SELECT plantIdx FROM UserPlantList WHERE userIdx=? AND status=?";
        Object[] Params = new Object[]{userIdx, "selected"};

        return this.jdbcTemplate.queryForObject(Query, int.class, Params);
    }

    //화분 선택 API ~ 기존에 선택되어있던 화분의 status를 active로 바꾸자 (selected -> active)
    public int activePlant(int userIdx) {
        String queryToActive = "UPDATE UserPlantList SET status=? " +
                "WHERE plantIdx=(SELECT Idx FROM (SELECT plantIdx AS Idx FROM UserPlantList WHERE userIdx=? AND status=?) T)";
        Object[] paramsToActive = new Object[]{"active", userIdx, "selected"};

        return this.jdbcTemplate.update(queryToActive, paramsToActive);
    }

    //화분 선택 API
    public int selectPlant(PatchSelectPlantReq patchSelectPlantReq) {
        //선택된 화분의 status를 selected로 바꾸자 (active -> selected)
        String Query = "UPDATE UserPlantList SET status=? WHERE userIdx=? AND plantIdx=?";
        Object[] Params = new Object[]{"selected", patchSelectPlantReq.getUserIdx(), patchSelectPlantReq.getPlantIdx()};

        return this.jdbcTemplate.update(Query, Params);
    }

    //화분 구매 API
    public int buyPlant(PostBuyPlantReq postBuyPlantReq) {
        String Query = "INSERT INTO UserPlantList(userIdx, plantIdx) VALUES(?, ?)";
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

    //화분 개수 조회 API
    public int countPlant() {
        String Query = "SELECT count(*) from Plant";
        return this.jdbcTemplate.queryForObject(Query, int.class);
    }

    //화분이 시무룩 상태인지 확인
    public boolean checkSad(int userIdx) {
        String Query = "SELECT isSad FROM User WHERE userIdx=?";
        int Param = userIdx;

        return this.jdbcTemplate.queryForObject(Query, boolean.class, Param);
    }

    //모든 화분 조회(Profile + 상점)
    public List<GetPlantRes> getPlantList(int userIdx, List<Integer> plantIdxList, List<Integer> userPlantIdxList) {

        List<GetPlantRes> getPlantResList = new ArrayList<GetPlantRes>();

        for (int i = 0; i < plantIdxList.size(); i++) {

            GetPlantRes getPlantRes = new GetPlantRes();

            String plantQuery = "SELECT plantIdx, plantName, plantInfo, plantPrice, maxLevel " +
                    "FROM Plant WHERE plantIdx=? AND status=?";
            Object[] plantParams = new Object[]{plantIdxList.get(i), "active"};

            //화분 기본 정보 FROM Plant
            PlantBasicInfo plantBasicInfo = jdbcTemplate.queryForObject(
                    plantQuery,
                    (rs, rowNum) -> new PlantBasicInfo(
                            rs.getInt("plantIdx"),
                            rs.getString("plantName"),
                            rs.getString("plantInfo"),
                            rs.getInt("plantPrice"),
                            rs.getInt("maxLevel")),
                    plantParams);

            // 화분 기본 정보 set
            getPlantRes.setPlantIdx(plantBasicInfo.getPlantIdx());
            getPlantRes.setPlantName(plantBasicInfo.getPlantName());
            getPlantRes.setPlantInfo(plantBasicInfo.getPlantInfo());
            getPlantRes.setPlantPrice(plantBasicInfo.getPlantPrice());
            getPlantRes.setMaxLevel(plantBasicInfo.getMaxLevel());


            int Idx = plantIdxList.get(i);
            int uIdx = userPlantIdxList.indexOf(plantIdxList.get(i)); //보유한 화분인지 idx로 구분

            //사용자가 보유중인 화분인 경우 현재 레벨, 상태(active/selected) set
            if (uIdx != -1) {
                String currentLevelQuery = "SELECT level FROM UserPlantList WHERE userIdx=? AND plantIdx=?";
                Object[] Params = new Object[]{userIdx, userPlantIdxList.get(uIdx)};
                int level = jdbcTemplate.queryForObject(currentLevelQuery, int.class, Params);

                // 유저의 보유중인 화분 상태(active/selected) 가져오기
                String plantStatusQuery = "SELECT status FROM UserPlantList WHERE userIdx=? AND plantIdx=?";
                String status = jdbcTemplate.queryForObject(plantStatusQuery, String.class, Params);

                getPlantRes.setCurrentLevel(level); //현재 레벨
                getPlantRes.setPlantStatus(status); //보유 상태
            } else {
                //사용자가 미보유한 화분인 경우
                getPlantRes.setPlantStatus("inactive");
                getPlantRes.setIsOwn(false);
            }
            getPlantResList.add(getPlantRes);
        }

        return getPlantResList;
    }

    //userIdx의 모든 plantIdx 목록
    public List<Integer> getUserPlantIdx(int userIdx) {
        String Query = "SELECT plantIdx FROM UserPlantList WHERE userIdx=?";
        int Param = userIdx;

        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> rs.getInt("plantIdx"),
                Param);
    }

    //Plant테이블의 모든 plantIdx 목록
    public List<Integer> getPlantIdx() {
        String Query = "SELECT plantIdx FROM Plant";

        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> rs.getInt("plantIdx"));
    }

    // =================================== 화분 점수 및 단계 변경 ===================================

    // 프리미엄 계정인가?
    public String isPremium(int userIdx) {
        String query = "SELECT isPremium FROM User WHERE userIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, userIdx);
    }

    // 화분 점수 반환
    public int getScore(int userIdx) {
        String query = "SELECT score FROM UserPlantList WHERE userIdx = ? AND status = 'selected'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 화분 단계 반환
    public int getLevel(int userIdx) {
        String query = "SELECT level FROM UserPlantList WHERE userIdx = ? AND status = 'selected'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 선택한 화분의 최대 단계 반환
    public int getMaxLevel(int userIdx) {
        String query = "SELECT Plant.maxLevel FROM UserPlantList " +
                "INNER JOIN Plant ON UserPlantList.plantIdx = Plant.plantIdx " +
                "WHERE UserPlantList.userIdx = ? AND UserPlantList.status = 'selected'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 화분 단계 수정
    public int setLevel(int userIdx, int level) {
        String query = "UPDATE UserPlantList SET level = ? WHERE userIdx = ? AND status = 'selected'";
        return this.jdbcTemplate.update(query, level, userIdx);
    }

    // 화분 점수 수정
    public int setScore(int userIdx, int score) {
        String query = "UPDATE UserPlantList SET score = ? WHERE userIdx = ? AND status = 'selected'";
        return this.jdbcTemplate.update(query, score, userIdx);
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

    //프리미엄 계정인지 확인
    public String checkPremium(int userIdx) {
        String Query = "SELECT isPremium FROM User WHERE userIdx=?";
        int Param = userIdx;

        return this.jdbcTemplate.queryForObject(Query, String.class, Param);
    }
     */

}
