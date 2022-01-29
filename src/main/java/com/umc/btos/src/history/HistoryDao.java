package com.umc.btos.src.history;

import com.umc.btos.config.Constant;
import com.umc.btos.src.history.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class HistoryDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 일기 & 편지 발신자 닉네임 목록 반환 (createdAt 기준 내림차순 정렬)
    public List<String> getNickNameList_sortedByCreatedAt(int userIdx) {
        String query = "SELECT DISTINCT senderNickName FROM (" +
                "SELECT User.nickName AS senderNickName, Diary.createdAt AS sendAt FROM User " +
                "INNER JOIN (DiarySendList INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx) ON User.userIdx = Diary.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "UNION " +
                "SELECT User.nickName AS senderNickName, Letter.createdAt AS sendAt FROM User " +
                "INNER JOIN (LetterSendList INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx) ON User.userIdx = Letter.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "ORDER BY sendAt DESC" +
                ") senderNickName";

        return this.jdbcTemplate.queryForList(query, String.class, userIdx, userIdx);
    }

    // --------------------------------------- null 확인 ---------------------------------------

    // Diary null 확인 : filtering = sender
    public int hasHistory_diary(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? ";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // Letter null 확인 : filtering = sender
    public int hasHistory_letter(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? ";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // Diary null 확인 : filtering = diary
    public int hasHistory_diary(int userIdx) {
        String query = "SELECT COUNT(*) " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? ";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // Letter null 확인 : filtering = letter
    public int hasHistory_letter(int userIdx) {
        String query = "SELECT COUNT(*) " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? ";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- List<History> 반환 ---------------------------------------

    // 일기 리스트 반환 : filtering = sender
    // DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName
    // provider 단에서의 연산을 줄이기 위해서 sendAt 기준 내림차순 정렬로 반환
    public List<History> getDiaryList(int userIdx, String senderNickName) {
        String query = "SELECT Diary.diaryIdx AS idx, User.nickName AS senderNickName," +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        new History_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 편지 리스트 반환 : filtering = sender
    // LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName
    // provider 단에서의 연산을 줄이기 위해서 sendAt 기준 내림차순 정렬로 반환
    public List<History> getLetterList(int userIdx, String senderNickName) {
        String query = "SELECT Letter.letterIdx AS idx, User.nickName AS senderNickName, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 일기 리스트 반환 : filtering = diary
    // DiarySendList.receiverIdx = userIdx
    public List<History> getDiaryList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT Diary.diaryIdx AS idx, User.nickName AS senderNickName," +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        new History_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, startData, endData);
    }

    // 편지 리스트 반환 : filtering = letter
    // LetterSendList.receiverIdx = userIdx
    public List<History> getLetterList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT Letter.letterIdx AS idx, User.nickName AS senderNickName, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), userIdx, startData, endData);
    }

    // --------------------------------------- data 개수 반환 ---------------------------------------

    // 일기 리스트 반환 시 (filtering = diary) data 개수 반환
    public int getDiaryList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM DiarySendList WHERE DiarySendList.receiverIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 리스트 반환 시 (filtering = letter) data 개수 반환
    public int getLetterList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM LetterSendList WHERE LetterSendList.receiverIdx = ? ";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- 인덱스 리스트 반환 ---------------------------------------

    // diaryIdx 리스트 반환 : filtering = sender && search
    public List<Integer> getDiaryIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? " +
                "ORDER BY sendAt DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // diaryIdx 리스트 반환 : filtering = diary && search
    public List<Integer> getDiaryIdxList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, startData, endData);
    }

    // letter 리스트 반환 : filtering = sender && search
    public List<Integer> getLetterIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? " +
                "ORDER BY sendAt DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // letter 리스트 반환 : filtering = letter && search
    public List<Integer> getLetterIdxList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, startData, endData);
    }

    // --------------------------------------- 인덱스 리스트 size 반환 ---------------------------------------

    // diaryIdx 리스트 반환 시 (filtering = diary) data 개수 반환
    public int getDiaryIdxList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM DiarySendList WHERE DiarySendList.receiverIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // letterIdx 리스트 반환 시 (filtering = letter) data 개수 반환
    public int getLetterIdxList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM LetterSendList WHERE LetterSendList.receiverIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- content 반환 ---------------------------------------

    // Diary.content 반환
    public String getDiaryContent(int diaryIdx) {
        String query = "SELECT content FROM Diary WHERE diaryIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
    }

    // Letter.content 반환
    public String getLetterContent(int letterIdx) {
        String query = "SELECT content FROM Letter WHERE letterIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, letterIdx);
    }

    // --------------------------------------- History 반환 ---------------------------------------

    // 일기 반환
    public History getDiary(int userIdx, int diaryIdx) {
        String query = "SELECT Diary.diaryIdx AS idx, User.nickName AS senderNickName," +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND Diary.diaryIdx = ?";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        new History_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, diaryIdx);
    }

    // 편지 반환
    public History getLetter(int userIdx, int letterIdx) {
        String query = "SELECT Letter.letterIdx AS idx, User.nickName AS senderNickName, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND Letter.letterIdx = ?";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), userIdx, letterIdx);
    }

}
