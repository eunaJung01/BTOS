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

    // 존재하는 회원인지 확인
    public int checkUserIdx(int userIdx) {
        String query = "SELECT EXISTS (SELECT userIdx FROM User WHERE userIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, userIdx);
    }

    // 존재하는 회원 닉네임인지 확인
    public int checkNickName(String nickName) {
        String query = "SELECT EXISTS (SELECT nickName FROM User WHERE nickName = ?)";
        return this.jdbcTemplate.queryForObject(query, int.class, nickName);
    }

    // 존재하는 일기인지 확인
    public int checkDiaryIdx(int diaryIdx) {
        String query = "SELECT EXISTS (SELECT diaryIdx FROM Diary WHERE diaryIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 존재하는 편지인지 확인
    public int checkLetterIdx(int letterIdx) {
        String query = "SELECT EXISTS (SELECT letterIdx FROM Letter WHERE letterIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, letterIdx);
    }

    // 존재하는 답장인지 확인
    public int checkReplyIdx(int replyIdx) {
        String query = "SELECT EXISTS (SELECT replyIdx FROM Reply WHERE replyIdx = ? AND status = 'active')";
        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
    }

    // ==============================================  History 목록 조회 ==============================================

    // 일기 & 편지 & 답장 발신인 닉네임 목록 개수 반환
    public int getNickNameList_num(int userIdx) {
        String query = "SELECT COUNT(DISTINCT senderNickName) " +
                "FROM ( " +
                // Diary
                "         SELECT User.nickName AS senderNickName, Diary.createdAt AS sendAt " +
                "         FROM User " +
                "                  INNER JOIN (DiarySendList INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx) " +
                "                             ON User.userIdx = Diary.userIdx " +
                "         WHERE DiarySendList.receiverIdx = ? " +
                "           AND Diary.isSend = 1 " +
                "           AND DiarySendList.status = 'active' " +
                "         UNION " +
                // Letter
                "         SELECT User.nickName AS senderNickName, Letter.createdAt AS sendAt " +
                "         FROM User " +
                "                  INNER JOIN (LetterSendList INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx) " +
                "                             ON User.userIdx = Letter.userIdx " +
                "         WHERE LetterSendList.receiverIdx = ? " +
                "           AND LetterSendList.status = 'active' " +
                "         UNION " +
                // Reply
                "         SELECT User.nickName AS senderNickName, Reply.createdAt As sendAt " +
                "         FROM Reply " +
                "                  INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "         WHERE Reply.receiverIdx = ? " +
                "           AND Reply.status = 'active' " +
//                "           AND User.status != 'system' " + // 저편너머 시스템 계정 표시 유무
                "     ) senderNickName";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, userIdx, userIdx);
    }

    // 일기 & 편지 & 답장 발신인 닉네임 목록 반환 (createdAt 기준 내림차순 정렬 + nickName search + 페이징 처리)
    public List<String> getNickNameList(int userIdx, String search, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        search = "%" + search + "%";

        String query = "SELECT DISTINCT senderNickName " +
                "FROM ( " +
                // Diary
                "         SELECT User.nickName AS senderNickName, Diary.createdAt AS sendAt " +
                "         FROM User " +
                "                  INNER JOIN (DiarySendList INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx) " +
                "                             ON User.userIdx = Diary.userIdx " +
                "         WHERE DiarySendList.receiverIdx = ? " +
                "           AND Diary.isSend = 1 " +
                "           AND DiarySendList.status = 'active' " +
                "           AND REPLACE(User.nickName, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "         UNION " +
                // Letter
                "         SELECT User.nickName AS senderNickName, Letter.createdAt AS sendAt " +
                "         FROM User " +
                "                  INNER JOIN (LetterSendList INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx) " +
                "                             ON User.userIdx = Letter.userIdx " +
                "         WHERE LetterSendList.receiverIdx = ? " +
                "           AND LetterSendList.status = 'active' " +
                "           AND REPLACE(User.nickName, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "         UNION " +
                // Reply
                "         SELECT User.nickName AS senderNickName, Reply.createdAt As sendAt " +
                "         FROM Reply " +
                "                  INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "         WHERE Reply.receiverIdx = ? " +
                "           AND Reply.status = 'active' " +
                "           AND REPLACE(User.nickName, ' ', '') LIKE REPLACE(?, ' ', '') " +
//                "           AND User.status != 'system' " + // 저편너머 시스템 계정 표시 유무
                "         ORDER BY sendAt DESC " + // createAt 기준 내림차순 정렬
                "     ) senderNickName " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.queryForList(query, String.class, userIdx, search, userIdx, search, userIdx, search, startData, Constant.HISTORY_DATA_NUM);
    }

    // 일기 null 확인 : filtering == sender
    public int hasHistory_diary(int userIdx, String senderNickName) {
        String query = "SELECT EXISTS(SELECT * " +
                "              FROM DiarySendList " +
                "                       INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                       INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "              WHERE DiarySendList.receiverIdx = ? " +
                "                AND User.nickName = ? " +
                "                AND Diary.isSend = 1 " +
                "                AND DiarySendList.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 수신한 일기의 개수
    public int getDiaryNum(int userIdx, String search) {
        search = "%" + search + "%";

        String query = "SELECT IF(EXISTS(SELECT * " +
                "                 FROM DiarySendList " +
                "                          INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                 WHERE DiarySendList.receiverIdx = ? " +
                "                   AND Diary.isSend = 1 " +
                "                   AND DiarySendList.status = 'active') = 0, " +
                "          0, " +
                "          (SELECT COUNT(*) " +
                "           FROM DiarySendList " +
                "                    INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "           WHERE DiarySendList.receiverIdx = ? " +
                "             AND Diary.isSend = 1 " +
                "             AND REPLACE(Diary.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "             AND DiarySendList.status = 'active'))";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, userIdx, search);
    }

    // 목록 조회 - filtering = diary
    public List<History> getDiaryList(int userIdx, String search, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        search = "%" + search + "%";

        String query = "SELECT Diary.diaryIdx                           AS typeIdx, " +
                "       Diary.content                            AS content, " +
                "       Diary.emotionIdx                         AS emotionIdx, " +
                "       DiarySendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active' " +
                "  AND REPLACE(Diary.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "ORDER BY sendAt_raw DESC " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, search, startData, Constant.HISTORY_DATA_NUM);
    }

    // History.senderActive
    public void setSenderActive(List<History> historyList) {
        String query = "SELECT CASE " +
                "           WHEN ? = 'diary' THEN (SELECT(IF((SELECT User.status " +
                "                                                   FROM DiarySendList " +
                "                                                            INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                                                            INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "                                                   WHERE Diary.diaryIdx = ? " +
                "                                                     AND Diary.isSend = 1 " +
                "                                                     AND DiarySendList.status = 'active' " +
                "                                                   GROUP BY User.status) = 'active', " +
                "                                                  1, " +
                "                                                  0))) " +
                "           WHEN ? = 'letter' THEN (SELECT(IF((SELECT User.status " +
                "                                                    FROM LetterSendList " +
                "                                                             INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "                                                             INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "                                                    WHERE Letter.letterIdx = ? " +
                "                                                      AND LetterSendList.status = 'active' " +
                "                                                    GROUP BY User.status) = 'active', " +
                "                                                   1, " +
                "                                                   0))) " +
                "           WHEN ? = 'reply' THEN (SELECT(IF((SELECT User.status " +
                "                                                   FROM Reply " +
                "                                                            INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "                                                   WHERE Reply.replyIdx = ? " +
                "                                                     AND Reply.status = 'active' " +
                "                                                   GROUP BY User.status) = 'active', " +
                "                                                  1, " +
                "                                                  0))) END";

        for (History history : historyList) {
            String type = history.getType();
            int typeIdx = history.getTypeIdx();
            history.setSenderActive(this.jdbcTemplate.queryForObject(query, boolean.class, type, typeIdx, type, typeIdx, type, typeIdx));
        }
    }

    // History.senderActive
    public void setSenderActive(History history) {
        String query = "SELECT CASE " +
                "           WHEN ? = 'diary' THEN (SELECT(IF((SELECT User.status " +
                "                                                   FROM DiarySendList " +
                "                                                            INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                                                            INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "                                                   WHERE Diary.diaryIdx = ? " +
                "                                                     AND Diary.isSend = 1 " +
                "                                                     AND DiarySendList.status = 'active' " +
                "                                                   GROUP BY User.status) = 'active', " +
                "                                                  1, " +
                "                                                  0))) " +
                "           WHEN ? = 'letter' THEN (SELECT(IF((SELECT User.status " +
                "                                                    FROM LetterSendList " +
                "                                                             INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "                                                             INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "                                                    WHERE Letter.letterIdx = ? " +
                "                                                      AND LetterSendList.status = 'active' " +
                "                                                    GROUP BY User.status) = 'active', " +
                "                                                   1, " +
                "                                                   0))) " +
                "           WHEN ? = 'reply' THEN (SELECT(IF((SELECT User.status " +
                "                                                   FROM Reply " +
                "                                                            INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "                                                   WHERE Reply.replyIdx = ? " +
                "                                                     AND Reply.status = 'active' " +
                "                                                   GROUP BY User.status) = 'active', " +
                "                                                  1, " +
                "                                                  0))) END";

        String type = history.getType();
        int typeIdx = history.getTypeIdx();
        history.setSenderActive(this.jdbcTemplate.queryForObject(query, boolean.class, type, typeIdx, type, typeIdx, type, typeIdx));
    }

    // History.doneListNum
    public int getDoneListNum(int diaryIdx) {
        String query = "SELECT COUNT(*) FROM Done WHERE diaryIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 수신한 편지 & 답장 개수
    public int getLetterNum(int userIdx, String search) {
        search = "%" + search + "%";

        String query = "SELECT COUNT(*) " +
                "FROM (SELECT Letter.letterIdx                          AS typeIdx, " +
                "             Letter.content                            AS content, " +
                "             LetterSendList.createdAt                  AS sendAt_raw, " +
                "             date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "             User.nickName                             AS senderNickName, " +
                "             User.fontIdx                              AS senderFontIdx " +
                "      FROM LetterSendList " +
                "               INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "               INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "      WHERE LetterSendList.receiverIdx = ? " +
                "        AND LetterSendList.status = 'active' " +
                "        AND REPLACE(Letter.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "      UNION " +
                "      SELECT Reply.replyIdx                           AS typeIdx, " +
                "             Reply.content                            AS content, " +
                "             Reply.createdAt                          AS sendAt_raw, " +
                "             date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "             User.nickName                            AS senderNickName, " +
                "             User.fontIdx                             AS senderFontIdx " +
                "      FROM Reply " +
                "               INNER JOIN User ON Reply.replierIdx = User.userIdx\n" +
                "      WHERE Reply.receiverIdx = ? " +
                "        AND Reply.status = 'active' " +
                "        AND REPLACE(Reply.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "      ORDER BY sendAt_raw DESC) temp";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, search, userIdx, search);
    }

    // 목록 조회 - filtering = letter
    public List<History> getLetterList(int userIdx, String search, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        search = "%" + search + "%";

        String query = "SELECT 'letter'                                  AS type, " +
                "       Letter.letterIdx                          AS typeIdx, " +
                "       Letter.content                            AS content, " +
                "       LetterSendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                             AS senderNickName, " +
                "       User.fontIdx                              AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "  AND REPLACE(Letter.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "UNION " +
                "SELECT 'reply'                                  AS type, " +
                "       Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content                            AS content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "  AND Reply.status = 'active' " +
                "  AND REPLACE(Reply.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "ORDER BY sendAt_raw DESC " +
                "LIMIT ?, ?";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        rs.getString("type"),
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, search, userIdx, search, startData, Constant.HISTORY_DATA_NUM);
    }

    //
    public int getHistoryListNum(int userIdx, String senderNickName) {
        String query = "SELECT COUNT(*) " +
                "FROM (SELECT Diary.diaryIdx AS typeIdx " +
                "      FROM DiarySendList " +
                "               INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "               INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "      WHERE DiarySendList.receiverIdx = ? " +
                "        AND Diary.isSend = 1 " +
                "        AND DiarySendList.status = 'active' " +
                "        AND User.nickName = ? " +
                "      UNION " +
                "      SELECT Letter.letterIdx AS typeIdx " +
                "      FROM LetterSendList " +
                "               INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "               INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "      WHERE LetterSendList.receiverIdx = ? " +
                "        AND LetterSendList.status = 'active' " +
                "        AND User.nickName = ? " +
                "      UNION " +
                "      SELECT Reply.replyIdx AS typeIdx " +
                "      FROM Reply " +
                "               INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "      WHERE Reply.receiverIdx = ? " +
                "        AND Reply.status = 'active'" +
                "        AND User.nickName = ?) temp";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName, userIdx, senderNickName, userIdx, senderNickName);
    }

    //
    public History getFirstContent(int userIdx, String senderNickName) {
        String query = "SELECT 'diary'                           AS type, " +
                "       Diary.diaryIdx                           AS typeIdx, " +
                "       Diary.content                            AS content, " +
                "       DiarySendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active'" +
                "  AND User.nickName = ?\n" +
                "UNION " +
                "SELECT 'letter'                                  AS type, " +
                "       Letter.letterIdx                          AS typeIdx, " +
                "       Letter.content                            AS content, " +
                "       LetterSendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                             AS senderNickName, " +
                "       User.fontIdx                              AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "  AND User.nickName = ?\n" +
                "UNION " +
                "SELECT 'Reply'                                  AS type, " +
                "       Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content                            AS content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "  AND Reply.status = 'active' " +
                "  AND User.nickName = ?\n" +
                "ORDER BY sendAt_raw DESC " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        rs.getString("type"),
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, senderNickName, userIdx, senderNickName, userIdx, senderNickName);
    }

    // =============================================  History 발신인 조회 =============================================

    // 편지 null 확인 : filtering == sender
    public int hasHistory_letter(int userIdx, String senderNickName) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "AND User.nickName = ? " +
                "AND LetterSendList.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // 답장 null 확인 : filtering == sender
    public int hasHistory_reply(int userIdx, String senderNickName) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "AND User.nickName = ? " +
                "AND Reply.status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName);
    }

    // ---------------------------------------------------------------------------------------------

    public boolean getSenderActive_diary(int diaryIdx) {
        String query =
                "SELECT User.status " +
                        "FROM DiarySendList " +
                        "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                        "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                        "WHERE Diary.diaryIdx = ? " +
                        "  AND Diary.isSend = 1 " +
                        "  AND DiarySendList.status = 'active' " +
                        "GROUP BY User.status";

        String senderStatus = this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
        return senderStatus.compareTo("deleted") != 0; // User.status = delete -> false
    }

    public boolean getSenderActive_letter(int letterIdx) {
        String query =
                "SELECT User.status " +
                        "FROM LetterSendList " +
                        "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                        "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                        "WHERE LetterSendList.letterIdx = ? " +
                        "  AND LetterSendList.status = 'active' " +
                        "GROUP BY User.status";

        String senderStatus = this.jdbcTemplate.queryForObject(query, String.class, letterIdx);
        return senderStatus.compareTo("deleted") != 0; // User.status = deleted -> false
    }

    public boolean getSenderActive_reply(int replyIdx) {
        String query =
                "SELECT User.status " +
                        "FROM Reply " +
                        "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                        "WHERE Reply.replyIdx = ? " +
                        "  AND Reply.status = 'active' ";

        String senderStatus = this.jdbcTemplate.queryForObject(query, String.class, replyIdx);
        return senderStatus.compareTo("deleted") != 0; // User.status = delete -> false
    }

    // --------------------------------------- List<History> ---------------------------------------
    // filtering == diary || letter (paging)

    // 일기 (DiarySendList.receiverIdx = userIdx)
    public History getDiary_done(int userIdx, int diaryIdx, boolean senderActive) {
        String query = "SELECT Diary.diaryIdx                                   AS typeIdx, " +
                "       Diary.content                                    AS content, " +
                "       Diary.emotionIdx                                 AS emotionIdx, " +
                "       COUNT(Done.diaryIdx)                             AS doneListNum, " +
                "       DiarySendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                                    AS senderNickName, " +
                "       User.fontIdx                                     AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "         INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.diaryIdx = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getInt("doneListNum"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, diaryIdx);
    }

    // 일기 (DiarySendList.receiverIdx = userIdx)
    public History getDiary_nonDone(int userIdx, int diaryIdx, boolean senderActive) {
        String query = "SELECT Diary.diaryIdx                                   AS typeIdx, " +
                "       Diary.content                                    AS content, " +
                "       Diary.emotionIdx                                 AS emotionIdx, " +
                "       DiarySendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                                    AS senderNickName, " +
                "       User.fontIdx                                     AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.diaryIdx = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        0,
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, diaryIdx);
    }

    // 일기 (DiarySendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public History getDiary_done(int userIdx, int diaryIdx, String senderNickName, boolean senderActive) {
        String query = "SELECT Diary.diaryIdx                                   AS typeIdx, " +
                "       Diary.content                                    AS content, " +
                "       Diary.emotionIdx                                 AS emotionIdx, " +
                "       COUNT(Done.diaryIdx)                             AS doneListNum, " +
                "       DiarySendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.fontIdx                                     AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "         INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.diaryIdx = ? " +
                "  AND User.nickName = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getInt("doneListNum"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        senderNickName,
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, diaryIdx, senderNickName);
    }

    // 일기 (DiarySendList.receiverIdx = userIdx)
    public History getDiary_nonDone(int userIdx, int diaryIdx, String senderNickName, boolean senderActive) {
        String query = "SELECT Diary.diaryIdx                                   AS typeIdx, " +
                "       Diary.content                                    AS content, " +
                "       Diary.emotionIdx                                 AS emotionIdx, " +
                "       DiarySendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.fontIdx                                     AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.diaryIdx = ? " +
                "  AND User.nickName = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        0,
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        senderNickName,
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, diaryIdx, senderNickName);
    }

    // 편지 (LetterSendList.receiverIdx = userIdx AND User.nickName = senderNickName)
    public List<History> getLetterList(int userIdx, String senderNickName) {
        String query = "SELECT Letter.letterIdx                                  AS typeIdx, " +
                "       Letter.content                                    AS content, " +
                "       LetterSendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                                     AS senderNickName, " +
                "       User.fontIdx                                      AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "  AND User.nickName = ? " +
                "  AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        senderNickName,
                        rs.getInt("senderFontIdx")
                ), userIdx, senderNickName);
    }

    // 답장 (Reply.receiverIdx = userIdx AND User.nickName = senderNickName)
    public List<History> getReplyList(int userIdx, String senderNickName) {
        String query = "SELECT Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content                            AS content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "  AND User.nickName = ? " +
                "  AND Reply.status = 'active' " +
                "ORDER BY sendAt DESC";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new History(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        senderNickName,
                        rs.getInt("senderFontIdx")
                ), userIdx, senderNickName);
    }

    // --------------------------------------- History ---------------------------------------

    // 편지
    public History getLetter(int userIdx, int letterIdx, boolean senderActive) {
        String query = "SELECT Letter.letterIdx                                  AS typeIdx, " +
                "       Letter.content                                    AS content, " +
                "       LetterSendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                                     AS senderNickName, " +
                "       User.fontIdx                                      AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "  AND Letter.letterIdx = ? " +
                "  AND LetterSendList.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, letterIdx);
    }

    // 답장
    public History getReply(int userIdx, int replyIdx, boolean senderActive) {
        String query = "SELECT Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content                            AS content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "  AND Reply.replyIdx = ? " +
                "  AND Reply.status = 'active'";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, replyIdx);
    }

    // --------------------------------------- idxList ---------------------------------------
    // search != null

    // diaryIdx 리스트 반환 : filtering = sender
    public List<Integer> getDiaryIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Diary.diaryIdx AS idx, DiarySendList.createdAt AS sendAt_raw " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? AND User.nickName = ? AND Diary.isSend = 1 AND DiarySendList.status = 'active' " +
                "ORDER BY sendAt_raw DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // letterIdx 리스트 반환 : filtering = sender
    public List<Integer> getLetterIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Letter.letterIdx AS idx, LetterSendList.createdAt AS sendAt_raw " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? AND User.nickName = ? AND LetterSendList.status = 'active' " +
                "ORDER BY sendAt_raw DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // replyIdx 리스트 반환 : filtering = sender
    public List<Integer> getReplyIdxList(int userIdx, String senderNickName) {
        String query = "SELECT idx FROM (" +
                "SELECT Reply.replyIdx AS idx, Reply.createdAt AS sendAt_raw " +
                "FROM Reply " +
                "INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? AND User.nickName = ? AND Reply.status = 'active' " +
                "ORDER BY sendAt_raw DESC) idx";

        return this.jdbcTemplate.queryForList(query, int.class, userIdx, senderNickName);
    }

    // --------------------------------------- content ---------------------------------------

    // Diary.content 반환
    public String getDiaryContent(int diaryIdx) {
        String query = "SELECT content FROM Diary WHERE diaryIdx = ? AND isSend = 1 AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, diaryIdx);
    }

    // Letter.content 반환
    public String getLetterContent(int letterIdx) {
        String query = "SELECT content FROM Letter WHERE letterIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, letterIdx);
    }

    // Reply.content 반환
    public String getReplyContent(int replyIdx) {
        String query = "SELECT content FROM Reply WHERE replyIdx = ? AND status = 'active'";
        return this.jdbcTemplate.queryForObject(query, String.class, replyIdx);
    }

    // ===================================  History 본문 조회 ===================================

    // --------------------------------------- 본문 ---------------------------------------

    // 일기
    public GetHistoryRes getDiary_main(int diaryIdx, boolean senderActive) {
        String query = "SELECT Diary.diaryIdx                                   AS typeIdx, " +
                "       Diary.content, " +
                "       Diary.emotionIdx, " +
                "       DiarySendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                                    AS senderNickName, " +
                "       User.fontIdx                                     AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE Diary.diaryIdx = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active' " +
                "GROUP BY Diary.diaryIdx";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), diaryIdx);
    }

    // 일기 done list
    public List<Done> getDoneList_main(int diaryIdx) {
        String query = "SELECT Done.doneIdx, Done.content " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE Diary.diaryIdx = ? AND Diary.isSend = 1 AND DiarySendList.status = 'active' " +
                "GROUP BY Done.doneIdx";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new Done(
                        rs.getInt("doneIdx"),
                        rs.getString("content")
                ), diaryIdx);
    }

    // 해당 일기 done list 유무 반환
    public int hasDone(int diaryIdx) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM Done " +
                "WHERE diaryIdx = ? " +
                "AND status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 해당 일기 done list 유무 반환
    public int hasDone(int diaryIdx, String senderNickName) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM Diary " +
                "INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "INNER JOIN Done ON Diary.diaryIdx = Done.diaryIdx " +
                "WHERE Diary.diaryIdx = ? AND User.nickName = ?)";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx, senderNickName);
    }

    // 편지
    public GetHistoryRes getLetter_main(int letterIdx, boolean senderActive) {
        String query = "SELECT Letter.letterIdx                                  AS typeIdx, " +
                "       Letter.content, " +
                "       LetterSendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                                     AS senderNickName, " +
                "       User.fontIdx                                      AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE Letter.letterIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "GROUP BY Letter.letterIdx";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), letterIdx);
    }

    // --------------------------------------- List<Reply> ---------------------------------------

    // 일기
    public List<GetHistoryRes> getReplyList_diary(int userIdx, int diaryIdx, boolean senderActive) {
        String query = "SELECT Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content,\n" +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'diary' " +
                "  AND DiarySendList.sendIdx = " +
                "      (SELECT DISTINCT DiarySendList.sendIdx " +
                "       FROM DiarySendList " +
                "                INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "       WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "         AND Diary.diaryIdx = ?)";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, userIdx, diaryIdx);
    }

    // 편지
    public List<GetHistoryRes> getReplyList_letter(int userIdx, int letterIdx, boolean senderActive) {
        String query = "SELECT Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'letter' " +
                "  AND LetterSendList.sendIdx = " +
                "      (SELECT DISTINCT LetterSendList.sendIdx " +
                "       FROM LetterSendList " +
                "                INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "                INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "       WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "         AND Letter.letterIdx = ?)";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), userIdx, userIdx, letterIdx);
    }

    // 일기
    public List<GetHistoryRes> getReplyList_diary(int userIdx, int diaryIdx) {
        String query = "SELECT Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'diary' " +
                "  AND DiarySendList.sendIdx = " +
                "      (SELECT DISTINCT DiarySendList.sendIdx " +
                "       FROM DiarySendList " +
                "                INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "       WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "         AND Diary.diaryIdx = ?)";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, userIdx, diaryIdx);
    }

    // 편지
    public List<GetHistoryRes> getReplyList_letter(int userIdx, int letterIdx) {
        String query = "SELECT Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'letter' " +
                "  AND LetterSendList.sendIdx = " +
                "      (SELECT DISTINCT LetterSendList.sendIdx " +
                "       FROM LetterSendList " +
                "                INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "                INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "       WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "         AND Letter.letterIdx = ?)";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, userIdx, letterIdx);
    }

    // --------------------------------------- idx ---------------------------------------

    // diaryIdx
    public int getDiaryIdx_main(int replyIdx) {
        String query = "SELECT DISTINCT Diary.diaryIdx " +
                "FROM DiarySendList " +
                "INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "WHERE Reply.sendIdx = (SELECT sendIdx FROM Reply WHERE replyIdx = ?)";

        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
    }

    // letterIdx
    public int getLetterIdx_main(int replyIdx) {
        String query = "SELECT DISTINCT Letter.letterIdx " +
                "FROM LetterSendList " +
                "INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "WHERE Reply.sendIdx = (SELECT sendIdx FROM Reply WHERE replyIdx = ?)";

        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
    }

    // --------------------------------------- firstHistoryType ---------------------------------------

    public String getHistoryType(int replyIdx) {
        String query = "SELECT firstHistoryType FROM Reply WHERE Reply.replyIdx = ?";
        return this.jdbcTemplate.queryForObject(query, String.class, replyIdx);
    }

    // --------------------------------------- hasReply ---------------------------------------
    public int hasReply_diary(int userIdx, int diaryIdx) {
        String query = "SELECT EXISTS(SELECT Reply.replyIdx                           AS typeIdx, " +
                "                     Reply.content, " +
                "                     Reply.createdAt                          AS sendAt_raw, " +
                "                     date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "                     User.nickName                            AS senderNickName, " +
                "                     User.fontIdx                             AS senderFontIdx " +
                "              FROM DiarySendList " +
                "                       INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                       INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "                       INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "              WHERE Reply.firstHistoryType = 'diary' " +
                "                AND DiarySendList.sendIdx = " +
                "                    (SELECT DISTINCT DiarySendList.sendIdx " +
                "                     FROM DiarySendList " +
                "                              INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "                              INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "                     WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "                       AND Diary.diaryIdx = ?))";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, userIdx, diaryIdx);
    }

    public int hasReply_letter(int userIdx, int letterIdx) {
        String query = "SELECT EXISTS(SELECT * " +
                "              FROM LetterSendList " +
                "                       INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "                       INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "                       INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "              WHERE Reply.firstHistoryType = 'letter' " +
                "                AND LetterSendList.sendIdx = " +
                "                    (SELECT DISTINCT LetterSendList.sendIdx " +
                "                     FROM LetterSendList " +
                "                              INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "                              INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "                     WHERE (Reply.replierIdx = ? OR Reply.receiverIdx = ?) " +
                "                       AND Letter.letterIdx = ?))";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, userIdx, letterIdx);
    }

}
