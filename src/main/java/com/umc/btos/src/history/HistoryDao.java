package com.umc.btos.src.history;

import com.umc.btos.config.Constant;
import com.umc.btos.src.diary.model.GetDoneRes;
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

    // ============================  History 목록 조회 & 발신인 조회 ============================
    // Diary.createdAt AS createdAt, DiarySendList.createdAt AS sendAt
    // Letter.createdAt AS createdAt, LetterSendList.createdAt AS sendAt

    // 일기 & 편지 발신자 닉네임 목록 반환 (createdAt 기준 내림차순 정렬)
    public List<String> getNickNameList_sortedByCreatedAt(int userIdx) {
        String query = "SELECT DISTINCT senderNickName FROM (" +
                "SELECT User.nickName AS senderNickName, DiarySendList.createdAt AS sendAt FROM User " +
                "INNER JOIN (DiarySendList INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx) ON User.userIdx = Diary.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active' " +
                "UNION " +
                "SELECT User.nickName AS senderNickName, Letter.createdAt AS sendAt FROM User " +
                "INNER JOIN (LetterSendList INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx) ON User.userIdx = Letter.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC" +
                ") senderNickName";

        return this.jdbcTemplate.queryForList(query, String.class, userIdx, userIdx);
    }

    // --------------------------------------- null 확인 ---------------------------------------

    // 일기 null 확인 : filtering == sender
    public int hasHistory_diary(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 편지 null 확인 : filtering == sender
    public int hasHistory_letter(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 일기 null 확인 : filtering == diary
    public int hasHistory_diary(int userIdx) {
        String query = "SELECT COUNT(*) " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 null 확인 : filtering == letter
    public int hasHistory_letter(int userIdx) {
        String query = "SELECT COUNT(*) " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- History_Sender 반환 ---------------------------------------
    // filtering == sender && search == null
    // createAt 기준 내림차순 정렬 시 상위 1번째 항목 반환

    // 일기 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public History_Sender getDiary_sender(int userIdx, String senderNickName) {
        String query = "SELECT Diary.diaryIdx AS idx, " +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History_Sender(
                        "diary",
                        rs.getInt("idx"),
                        new HistoryContent_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public History_Sender getLetter_sender(int userIdx, String senderNickName) {
        String query = "SELECT Letter.letterIdx AS idx, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History_Sender(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // --------------------------------------- History_Sender 반환 ---------------------------------------
    // filtering = sender && search != null

    // 일기
    public History_Sender getDiary_sender(int userIdx, int diaryIdx) {
        String query = "SELECT Diary.diaryIdx AS idx, " +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND Diary.diaryIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History_Sender(
                        "diary",
                        rs.getInt("idx"),
                        new HistoryContent_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, diaryIdx);
    }

    // 편지
    public History_Sender getLetter_sender(int userIdx, int letterIdx) {
        String query = "SELECT Letter.letterIdx AS idx, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND Letter.letterIdx = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History_Sender(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), userIdx, letterIdx);
    }

    // --------------------------------------- List<History_Sender> 반환 ---------------------------------------
    // filtering == sender
    // provider 단에서의 연산을 줄이기 위해서 sendAt 기준 내림차순 정렬로 반환

    // 일기 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public List<History_Sender> getDiaryList_sender(int userIdx, String senderNickName) {
        String query = "SELECT Diary.diaryIdx AS idx, " +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History_Sender(
                        "diary",
                        rs.getInt("idx"),
                        new HistoryContent_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public List<History_Sender> getLetterList_sender(int userIdx, String senderNickName) {
        String query = "SELECT Letter.letterIdx AS idx, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History_Sender(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), userIdx, senderNickName);
    }

    // --------------------------------------- List<History_Sender> size 반환 ---------------------------------------
    // filtering == sender && search == null

    // 일기 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public int getDiaryListSize_sender(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public int getLetterListSize_sender(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // --------------------------------------- List<History> 반환 ---------------------------------------
    // filtering == diary || letter (paging)

    // 일기 (DiarySendList.receiverIdx = userIdx)
    public List<History> getDiaryList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT Diary.diaryIdx AS idx, User.nickName AS senderNickName, " +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        new HistoryContent_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, startData, endData);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx)
    public List<History> getLetterList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT Letter.letterIdx AS idx, User.nickName AS senderNickName, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active' " +
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

    // --------------------------------------- List<History> size 반환 ---------------------------------------

    // 일기 (filtering = diary)
    public int getDiaryList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM DiarySendList WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 편지 (filtering = letter)
    public int getLetterList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM LetterSendList WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- History 반환 ---------------------------------------

    // 일기
    public History getDiary(int userIdx, int diaryIdx) {
        String query = "SELECT Diary.diaryIdx AS idx, User.nickName AS senderNickName," +
                "Diary.content AS diaryContent, Diary.emotionIdx AS emotionIdx, COUNT(Done.diaryIdx) AS doneListNum, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND Diary.diaryIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        new HistoryContent_Diary(rs.getString("diaryContent"), rs.getInt("emotionIdx"), rs.getInt("doneListNum")),
                        rs.getString("sendAt")
                ), userIdx, diaryIdx);
    }

    // 편지
    public History getLetter(int userIdx, int letterIdx) {
        String query = "SELECT Letter.letterIdx AS idx, User.nickName AS senderNickName, Letter.content AS content, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND Letter.letterIdx = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("idx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), userIdx, letterIdx);
    }

    // --------------------------------------- 인덱스 리스트 반환 ---------------------------------------
    // search != null

    // diaryIdx 리스트 반환 : filtering = sender
    public List<Integer> getDiaryIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // letter 리스트 반환 : filtering = sender
    public List<Integer> getLetterIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // diaryIdx 리스트 반환 : filtering = diary
    public List<Integer> getDiaryIdxList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, startData, endData);
    }

    // letter 리스트 반환 : filtering = letter
    public List<Integer> getLetterIdxList(int userIdx, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        int endData = pageNum * Constant.HISTORY_DATA_NUM;

        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, startData, endData);
    }

    // --------------------------------------- 인덱스 리스트 size 반환 ---------------------------------------

    // diaryIdx 리스트 반환 시 (filtering = diary) data 개수 반환
    public int getDiaryIdxList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM DiarySendList WHERE DiarySendList.receiverIdx = ? AND DiarySendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // letterIdx 리스트 반환 시 (filtering = letter) data 개수 반환
    public int getLetterIdxList_dataNum(int userIdx) {
        String query = "SELECT COUNT(*) FROM LetterSendList WHERE LetterSendList.receiverIdx = ? AND LetterSendList.status = 'active'";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // --------------------------------------- 인덱스 반환 ---------------------------------------
    // filtering = sender && search != null

    // diaryIdx (createAt 기준 내림차순 정렬 시 상위 1번째 항목)
    public int getDiaryIdx_sender(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, Diary.createdAt AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // letterIdx (createAt 기준 내림차순 정렬 시 상위 1번째 항목)
    public int getLetterIdx_sender(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, Letter.createdAt AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC) idx " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
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

    // ============================  History 본문 조회 ============================

    // 본문 - 일기
    public Diary getDiary_main(int diaryIdx) {
        String query = "SELECT Diary.diaryIdx, Diary.emotionIdx, Diary.content, User.nickName AS senderNickName, date_format(DiarySendList.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE Diary.diaryIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new Diary(
                        rs.getInt("diaryIdx"),
                        rs.getInt("emotionIdx"),
                        rs.getString("content"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt")
                ), diaryIdx);
    }

    // 본문 - 일기 done list
    public List<Done> getDoneList_main(int diaryIdx) {
        String query = "SELECT Done.doneIdx, Done.content " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE Diary.diaryIdx = ? AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Done(
                        rs.getInt("doneIdx"),
                        rs.getString("content")
                ), diaryIdx);
    }

    // 본문 - 편지
    public Letter getLetter_main(int diaryIdx) {
        String query = "SELECT Letter.letterIdx AS idx, Letter.content AS content, User.nickName AS senderNickName, date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE Letter.letterIdx = ? AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new Letter(
                        rs.getInt("idx"),
                        rs.getString("content"),
                        rs.getString("senderNickName"),
                        rs.getString("sendAt")
                ), diaryIdx);
    }

    // 일기 답장 List
    public List<Reply> getReplyList_diary(int diaryIdx) {
        String query = "SELECT Reply.replyIdx, User.nickName AS senderNickName, Reply.content, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM Reply " +
                "INNER JOIN (Diary INNER JOIN DiarySendList ON Diary.diaryIdx = DiarySendList.diaryIdx) ON Reply.firstHistoryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'diary' AND Reply.firstHistoryIdx = ? " +
                "ORDER BY Reply.createdAt ASC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Reply(
                        rs.getInt("replyIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), diaryIdx);
    }

    // 편지 답장 List
    public List<Reply> getReplyList_letter(int letterIdx) {
        String query = "SELECT Reply.replyIdx, User.nickName AS senderNickName, Reply.content, date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt " +
                "FROM Reply " +
                "INNER JOIN (Letter INNER JOIN LetterSendList ON Letter.letterIdx = LetterSendList.letterIdx) ON Reply.firstHistoryIdx = Letter.letterIdx " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'letter' AND Reply.firstHistoryIdx = ? " +
                "ORDER BY Reply.createdAt ASC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Reply(
                        rs.getInt("replyIdx"),
                        rs.getString("senderNickName"),
                        rs.getString("content"),
                        rs.getString("sendAt")
                ), letterIdx);
    }

}
