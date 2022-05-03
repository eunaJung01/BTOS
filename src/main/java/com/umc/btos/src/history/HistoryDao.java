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

    // ---------------------------------------------------- 발신인 ----------------------------------------------------

    // 수신한 모든 항목(일기, 편지, 답장)에 대한 발신인 명수 반환 (닉네임 검색)
    public int getNickNameNum(int userIdx, String search) {
        search = "%" + search + "%";

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
                "     ) senderNickName";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, search, userIdx, search, userIdx, search);
    }

    // 수신한 모든 항목(일기, 편지, 답장)에 대한 발신인 닉네임 목록 반환 (createdAt 기준 내림차순 정렬 + 닉네임 검색 + 페이징 처리)
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

    // 해당 발신인에게서 수신한 모든 항목(일기, 편지, 답장)의 개수
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

    // 해당 발신인에게서 수신한 모든 항목(일기, 편지, 답장) 중 가장 최근에 받은 값 반환
    public History getFirstContent(int userIdx, String senderNickName) {
        String query = "SELECT 'diary'                           AS type, " +
                "       Diary.diaryIdx                           AS typeIdx, " +
                "       Diary.content                            AS content, " +
                "       DiarySendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active'" +
                "  AND User.nickName = ? " +
                "UNION " +
                "SELECT 'letter'                                  AS type, " +
                "       Letter.letterIdx                          AS typeIdx, " +
                "       Letter.content                            AS content, " +
                "       LetterSendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                              AS senderIdx, " +
                "       User.nickName                             AS senderNickName, " +
                "       User.fontIdx                              AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "  AND User.nickName = ? " +
                "UNION " +
                "SELECT 'reply'                                  AS type, " +
                "       Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content                            AS content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "  AND Reply.status = 'active' " +
                "  AND User.nickName = ? " +
                "ORDER BY sendAt_raw DESC " +
                "LIMIT 1";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new History(
                        rs.getString("type"),
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, senderNickName, userIdx, senderNickName, userIdx, senderNickName);
    }

    // ---------------------------------------------------- 일기만 ----------------------------------------------------

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

    // History 목록 조회 (filtering = diary)
    public List<History> getDiaryList(int userIdx, String search, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        search = "%" + search + "%";

        String query = "SELECT Diary.diaryIdx                           AS typeIdx, " +
                "       Diary.content                            AS content, " +
                "       Diary.emotionIdx                         AS emotionIdx, " +
                "       DiarySendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
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
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, search, startData, Constant.HISTORY_DATA_NUM);
    }

    // Diary.emotionIdx 반환
    public int getEmotionIdx(int diaryIdx) {
        String query = "SELECT emotionIdx FROM Diary WHERE diaryIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // 해당 일기의 done list 유무 반환
    public int hasDone(int diaryIdx) {
        String query = "SELECT EXISTS(SELECT * " +
                "FROM Done " +
                "WHERE diaryIdx = ? " +
                "AND status = 'active')";

        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx); // 존재할 경우 1, 존재하지 않을 경우 0
    }

    // done list 개수 반환
    public int getDoneListNum(int diaryIdx) {
        String query = "SELECT COUNT(*) FROM Done WHERE diaryIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, diaryIdx);
    }

    // ---------------------------------------------------- 편지만 ----------------------------------------------------

    // 수신한 편지 & 답장의 개수 반환
    public int getLetterNum(int userIdx, String search) {
        search = "%" + search + "%";

        String query = "SELECT COUNT(*) " +
                "FROM (SELECT Letter.letterIdx                          AS typeIdx, " +
                "             Letter.content                            AS content, " +
                "             LetterSendList.createdAt                  AS sendAt_raw " +
                "      FROM LetterSendList " +
                "               INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "               INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "      WHERE LetterSendList.receiverIdx = ? " +
                "        AND LetterSendList.status = 'active' " +
                "        AND REPLACE(Letter.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "      UNION " +
                "      SELECT Reply.replyIdx                           AS typeIdx, " +
                "             Reply.content                            AS content, " +
                "             Reply.createdAt                          AS sendAt_raw " +
                "      FROM Reply " +
                "               INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "      WHERE Reply.receiverIdx = ? " +
                "        AND Reply.status = 'active' " +
                "        AND REPLACE(Reply.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "      ORDER BY sendAt_raw DESC) temp";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, search, userIdx, search);
    }

    // History 목록 조회 (filtering = letter)
    public List<History> getLetterList(int userIdx, String search, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        search = "%" + search + "%";

        String query = "SELECT 'letter'                                  AS type, " +
                "       Letter.letterIdx                          AS typeIdx, " +
                "       Letter.content                            AS content, " +
                "       LetterSendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                              AS senderIdx, " +
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
                "       User.userIdx                             AS senderIdx, " +
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
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, search, userIdx, search, startData, Constant.HISTORY_DATA_NUM);
    }

    // ------------------------------------------- set History.senderActive -------------------------------------------

    // List<History>
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

    // History
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

    public boolean setSenderActive(String type, int typeIdx) {
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

        return this.jdbcTemplate.queryForObject(query, boolean.class, type, typeIdx, type, typeIdx, type, typeIdx);
    }

    // =============================================  History 발신인 조회 =============================================

    // 해당 발신인에게서 수신한 모든 항목(일기, 편지, 답장)의 개수 (문자열 검색)
    public int getHistoryListNum_sender(int userIdx, String senderNickName, String search) {
        search = "%" + search + "%";

        String query = "SELECT COUNT(*) " +
                "FROM (SELECT Diary.diaryIdx AS typeIdx, " +
                "             Diary.content  AS content, " +
                "             User.nickName  AS senderNickName " +
                "      FROM DiarySendList " +
                "               INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "               INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "      WHERE DiarySendList.receiverIdx = ? " +
                "        AND Diary.isSend = 1 " +
                "        AND DiarySendList.status = 'active' " +
                "        AND User.nickName = ? " +
                "        AND REPLACE(Diary.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "      UNION " +
                "      SELECT Letter.letterIdx AS typeIdx, " +
                "             Letter.content   AS content, " +
                "             User.nickName    AS senderNickName " +
                "      FROM LetterSendList " +
                "               INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "               INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "      WHERE LetterSendList.receiverIdx = ? " +
                "        AND LetterSendList.status = 'active' " +
                "        AND User.nickName = ? " +
                "        AND REPLACE(Letter.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "      UNION " +
                "      SELECT Reply.replyIdx AS typeIdx, " +
                "             Reply.content  AS content, " +
                "             User.nickName  AS senderNickName " +
                "      FROM Reply " +
                "               INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "      WHERE Reply.receiverIdx = ? " +
                "        AND Reply.status = 'active' " +
                "        AND User.nickName = ? " +
                "        AND REPLACE(Reply.content, ' ', '') LIKE REPLACE(?, ' ', '')) temp";

        return this.jdbcTemplate.queryForObject(query, int.class, userIdx, senderNickName, search, userIdx, senderNickName, search, userIdx, senderNickName, search);
    }

    // 해당 발신인에게서 수신한 모든 항목(일기, 편지, 답장) 목록 (createdAt 기준 내림차순 정렬 + 문자열 검색 + 페이징 처리)
    public List<History> getHistoryList_sender(int userIdx, String senderNickName, String search, int pageNum) {
        int startData = (pageNum - 1) * Constant.HISTORY_DATA_NUM;
        search = "%" + search + "%";

        String query = "SELECT 'diary'                           AS type, " +
                "       Diary.diaryIdx                           AS typeIdx, " +
                "       Diary.content                            AS content, " +
                "       DiarySendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN User ON Diary.userIdx = User.userIdx " +
                "WHERE DiarySendList.receiverIdx = ? " +
                "  AND Diary.isSend = 1 " +
                "  AND DiarySendList.status = 'active' " +
                "  AND User.nickName = ? " +
                "  AND REPLACE(Diary.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "UNION " +
                "SELECT 'letter'                                  AS type, " +
                "       Letter.letterIdx                          AS typeIdx, " +
                "       Letter.content                            AS content, " +
                "       LetterSendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                              AS senderIdx, " +
                "       User.nickName                             AS senderNickName, " +
                "       User.fontIdx                              AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE LetterSendList.receiverIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "  AND User.nickName = ? " +
                "  AND REPLACE(Letter.content, ' ', '') LIKE REPLACE(?, ' ', '') " +
                "UNION " +
                "SELECT 'reply'                                  AS type, " +
                "       Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content                            AS content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.receiverIdx = ? " +
                "  AND Reply.status = 'active' " +
                "  AND User.nickName = ? " +
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
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), userIdx, senderNickName, search, userIdx, senderNickName, search, userIdx, senderNickName, search, startData, Constant.HISTORY_DATA_NUM);
    }

    // ==============================================  History 본문 조회 ==============================================

    // Reply.replierIdx 반환
    public int getReplierIdx(int replyIdx) {
        String query = "SELECT replierIdx FROM Reply WHERE replyIdx = ?";
        return this.jdbcTemplate.queryForObject(query, int.class, replyIdx);
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

    // --------------------------------------- 본문 ---------------------------------------

    // 일기
    public GetHistoryRes getDiary_main(int receiverIdx, int diaryIdx, int replyIdx, boolean senderActive) {
        String userIdx_query = "select Diary.userIdx from Diary where diaryIdx = ?";
        int userIdx = this.jdbcTemplate.queryForObject(userIdx_query, int.class, diaryIdx);

        String sendIdx_query;
        int sendIdx;

        // 일기 발신인일 경우
        if (userIdx == receiverIdx) {
            sendIdx_query = "select DiarySendList.sendIdx " +
                    "from Diary " +
                    "         inner join DiarySendList on Diary.diaryIdx = DiarySendList.diaryIdx " +
                    "where Diary.diaryIdx = ? " +
                    "  and receiverIdx = (select Reply.replierIdx from Reply where Reply.replyIdx = ?) " +
                    "  and DiarySendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, diaryIdx, replyIdx);
        }
        // 일기 수신인일 경우
        else {
            sendIdx_query = "select DiarySendList.sendIdx " +
                    "from Diary inner join DiarySendList on Diary.diaryIdx = DiarySendList.diaryIdx " +
                    "where Diary.diaryIdx = ? and receiverIdx = ? and DiarySendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, diaryIdx, receiverIdx);
        }

        String query = "SELECT Diary.diaryIdx                                   AS typeIdx, " +
                "       Diary.content, " +
                "       Diary.emotionIdx, " +
                "       DiarySendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                                     AS senderIdx, " +
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
                        sendIdx,
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), diaryIdx);
    }

    public GetHistoryRes getDiary_main(int receiverIdx, int diaryIdx, boolean senderActive) {
        String sendIdx_query = "select DiarySendList.sendIdx " +
                "from Diary inner join DiarySendList on Diary.diaryIdx = DiarySendList.diaryIdx " +
                "where Diary.diaryIdx = ? and receiverIdx = ? and DiarySendList.status = 'active'";
        int sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, diaryIdx, receiverIdx);
        System.out.println(sendIdx);

        String query = "SELECT Diary.diaryIdx                                   AS typeIdx, " +
                "       Diary.content, " +
                "       Diary.emotionIdx, " +
                "       DiarySendList.createdAt                          AS sendAt_raw, " +
                "       date_format(Diary.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                                     AS senderIdx, " +
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
                        sendIdx,
                        "diary",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getInt("emotionIdx"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), diaryIdx);
    }

    // 일기 done list
    public List<Done> getDoneList_main(int diaryIdx) {
        // done list 유무 확인
        String hasDone_query = "SELECT EXISTS(SELECT *  FROM Done  WHERE diaryIdx = ?  AND status = 'active')";
        int hasDone = this.jdbcTemplate.queryForObject(hasDone_query, int.class, diaryIdx); // done list 존재할 경우 1, 존재하지 않을 경우 0

        // 해당 일기에 done list가 있는 경우
        if (hasDone == 1) {
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
        // 해당 일기에 done list가 없는 경우
        else {
            return null;
        }
    }

    // 편지
    public GetHistoryRes getLetter_main(int receiverIdx, int letterIdx, int replyIdx, boolean senderActive) {
        String userIdx_query = "select Letter.userIdx from Letter where letterIdx = ?";
        int userIdx = this.jdbcTemplate.queryForObject(userIdx_query, int.class, letterIdx);

        String sendIdx_query;
        int sendIdx;

        // 편지 발신인일 경우
        if (userIdx == receiverIdx) {
            sendIdx_query = "select LetterSendList.sendIdx " +
                    "from Letter " +
                    "         inner join LetterSendList on Letter.letterIdx = LetterSendList.letterIdx " +
                    "where Letter.letterIdx = ? " +
                    "  and receiverIdx = (select Reply.replierIdx from Reply where Reply.replyIdx = ?) " +
                    "  and LetterSendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, letterIdx, replyIdx);
        }
        // 편지 수신인일 경우
        else {
            sendIdx_query = "select LetterSendList.sendIdx " +
                    "from Letter inner join LetterSendList on Letter.letterIdx = LetterSendList.letterIdx " +
                    "where Letter.letterIdx = ? and receiverIdx = ? and LetterSendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, letterIdx, receiverIdx);
        }

        String query = "SELECT Letter.letterIdx                   AS typeIdx, " +
                "       Letter.content, " +
                "       LetterSendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                              AS senderIdx, " +
                "       User.nickName                             AS senderNickName, " +
                "       User.fontIdx                              AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE Letter.letterIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "GROUP BY Letter.letterIdx";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "letter",
                        sendIdx,
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), letterIdx);
    }

    // 편지
    public GetHistoryRes getLetter_main(int receiverIdx, int letterIdx, boolean senderActive) {
        String sendIdx_query = "select LetterSendList.sendIdx " +
                "from Letter inner join LetterSendList on Letter.letterIdx = LetterSendList.letterIdx " +
                "where Letter.letterIdx = ? and receiverIdx = ? and LetterSendList.status = 'active'";
        int sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, letterIdx, receiverIdx);


        String query = "SELECT Letter.letterIdx                   AS typeIdx, " +
                "       Letter.content, " +
                "       LetterSendList.createdAt                  AS sendAt_raw, " +
                "       date_format(Letter.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                              AS senderIdx, " +
                "       User.nickName                             AS senderNickName, " +
                "       User.fontIdx                              AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN User ON Letter.userIdx = User.userIdx " +
                "WHERE Letter.letterIdx = ? " +
                "  AND LetterSendList.status = 'active' " +
                "GROUP BY Letter.letterIdx";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "letter",
                        sendIdx,
                        "letter",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        senderActive,
                        rs.getInt("senderFontIdx")
                ), letterIdx);
    }

    // --------------------------------------- List<Reply> ---------------------------------------

    // 일기
    public List<GetHistoryRes> getReplyList_diary(int userIdx, String firstHistoryType, int replyIdx, int diaryIdx) {
        String diary_userIdx_query = "select userIdx from Diary where diaryIdx = ?";
        int diary_userIdx = this.jdbcTemplate.queryForObject(diary_userIdx_query, int.class, diaryIdx);

        String sendIdx_query;
        int sendIdx;

        // 일기 발신인일 경우
        if (userIdx == diary_userIdx) {
            sendIdx_query = "select DiarySendList.sendIdx " +
                    "from Diary " +
                    "         inner join DiarySendList on Diary.diaryIdx = DiarySendList.diaryIdx " +
                    "where Diary.diaryIdx = ? " +
                    "  and receiverIdx = (select Reply.replierIdx from Reply where Reply.replyIdx = ?) " +
                    "  and DiarySendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, diaryIdx, replyIdx);
        }
        // 일기 수신인일 경우
        else {
            sendIdx_query = "select DiarySendList.sendIdx " +
                    "from Diary inner join DiarySendList on Diary.diaryIdx = DiarySendList.diaryIdx " +
                    "where Diary.diaryIdx = ? and receiverIdx = ? and DiarySendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, diaryIdx, userIdx);
        }

        String query = "SELECT Reply.replyIdx                    AS typeIdx, " +
                "       Reply.content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'diary' " +
                "  AND Reply.sendIdx = ? " +
                "order by Reply.createdAt asc";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        firstHistoryType,
                        sendIdx,
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), sendIdx);
    }

    public List<GetHistoryRes> getReplyList_diary(int userIdx, String firstHistoryType, int diaryIdx) {
        String sendIdx_query = "select DiarySendList.sendIdx " +
                "from Diary inner join DiarySendList on Diary.diaryIdx = DiarySendList.diaryIdx " +
                "where Diary.diaryIdx = ? and receiverIdx = ? and DiarySendList.status = 'active'";
        int sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, diaryIdx, userIdx);

        String query = "SELECT Reply.replyIdx                    AS typeIdx, " +
                "       Reply.content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM DiarySendList " +
                "         INNER JOIN Diary ON DiarySendList.diaryIdx = Diary.diaryIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = DiarySendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'diary' " +
                "  AND Reply.sendIdx = ? " +
                "order by Reply.createdAt asc";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        firstHistoryType,
                        sendIdx,
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), sendIdx);
    }

    // 편지
    public List<GetHistoryRes> getReplyList_letter(int userIdx, String firstHistoryType, int replyIdx, int letterIdx) {
        String letter_userIdx_query = "select userIdx from Letter where letterIdx = ?";
        int letter_userIdx = this.jdbcTemplate.queryForObject(letter_userIdx_query, int.class, letterIdx);

        String sendIdx_query;
        int sendIdx;

        // 편지 발신인일 경우
        if (userIdx == letter_userIdx) {
            sendIdx_query = "select LetterSendList.sendIdx " +
                    "from Letter " +
                    "         inner join LetterSendList on Letter.letterIdx = LetterSendList.letterIdx " +
                    "where Letter.letterIdx = ? " +
                    "  and receiverIdx = (select Reply.replierIdx from Reply where Reply.replyIdx = ?) " +
                    "  and LetterSendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, letterIdx, replyIdx);
        }
        // 편지 수신인일 경우
        else {
            sendIdx_query = "select LetterSendList.sendIdx " +
                    "from Letter inner join LetterSendList on Letter.letterIdx = LetterSendList.letterIdx " +
                    "where Letter.letterIdx = ? and receiverIdx = ? and LetterSendList.status = 'active'";
            sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, letterIdx, userIdx);
        }

        String query = "SELECT Reply.replyIdx                    AS typeIdx, " +
                "       Reply.content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'letter' " +
                "  AND Reply.sendIdx = ? " +
                "order by Reply.createdAt asc";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        firstHistoryType,
                        sendIdx,
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), sendIdx);
    }

    public List<GetHistoryRes> getReplyList_letter(int userIdx, String firstHistoryType, int letterIdx) {
        String sendIdx_query = "select LetterSendList.sendIdx " +
                "from Letter inner join LetterSendList on Letter.letterIdx = LetterSendList.letterIdx " +
                "where Letter.letterIdx = ? and receiverIdx = ? and LetterSendList.status = 'active'";
        int sendIdx = this.jdbcTemplate.queryForObject(sendIdx_query, int.class, letterIdx, userIdx);

        String query = "SELECT Reply.replyIdx                    AS typeIdx, " +
                "       Reply.content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM LetterSendList " +
                "         INNER JOIN Letter ON LetterSendList.letterIdx = Letter.letterIdx " +
                "         INNER JOIN Reply ON Reply.sendIdx = LetterSendList.sendIdx " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.firstHistoryType = 'letter' " +
                "  AND Reply.sendIdx = ? " +
                "order by Reply.createdAt asc";

        return this.jdbcTemplate.query(query,
                (rs, rowNum) -> new GetHistoryRes(
                        firstHistoryType,
                        sendIdx,
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), sendIdx);
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

    public GetHistoryRes getReply_systemMail(int replyIdx) {
        String query = "SELECT 'reply'                           AS type, " +
                "       Reply.replyIdx                           AS typeIdx, " +
                "       Reply.content                            AS content, " +
                "       Reply.createdAt                          AS sendAt_raw, " +
                "       date_format(Reply.createdAt, '%Y.%m.%d') AS sendAt, " +
                "       User.userIdx                             AS senderIdx, " +
                "       User.nickName                            AS senderNickName, " +
                "       User.fontIdx                             AS senderFontIdx " +
                "FROM Reply " +
                "         INNER JOIN User ON Reply.replierIdx = User.userIdx " +
                "WHERE Reply.replyIdx = ?";

        return this.jdbcTemplate.queryForObject(query,
                (rs, rowNum) -> new GetHistoryRes(
                        "reply",
                        rs.getInt("typeIdx"),
                        rs.getString("content"),
                        rs.getString("sendAt_raw"),
                        rs.getString("sendAt"),
                        rs.getInt("senderIdx"),
                        rs.getString("senderNickName"),
                        rs.getInt("senderFontIdx")
                ), replyIdx);
    }

}
