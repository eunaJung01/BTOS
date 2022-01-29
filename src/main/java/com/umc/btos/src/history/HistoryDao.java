package com.umc.btos.src.history;

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

    // null 확인 : Diary
    public int hasHistory_diary(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? ";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // null 확인 : Letter
    public int hasHistory_letter(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? ";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 일기 리스트 반환 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
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

    // 편지 리스트 반환 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
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

}
