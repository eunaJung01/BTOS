package com.umc.btos.src.diary;

import com.umc.btos.src.diary.model.PostDiaryReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DiaryDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDateSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 일기 저장 -> diaryIdx 반환
    public int saveDiary(PostDiaryReq postDiaryReq) {
        String query = "INSERT INTO Diary(userIdx, emotionIdx, diaryDate, isPublic, content) VALUES(?,?,?,?,?)";
        Object[] diary = new Object[]{
                postDiaryReq.getUserIdx(), postDiaryReq.getEmotionIdx(), postDiaryReq.getDiaryDate(), postDiaryReq.getIsPublic(), postDiaryReq.getDiaryContent()
        };
        this.jdbcTemplate.update(query, diary);

        String get_diaryIdx_query = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(get_diaryIdx_query, int.class);
    }

    // Done List 저장 -> doneIdxList 반환
    public List saveDoneList(int diaryIdx, List doneList) {
        List doneListIdx = new ArrayList(); // doneIdx를 담는 배열

        String query = "INSERT INTO Done(diaryIdx, content) VALUES(?,?)";
        for (Object doneContent : doneList) {
            Object[] done = new Object[]{
                    diaryIdx, doneContent
            };
            this.jdbcTemplate.update(query, done); // Done Table에 순차적으로 저장

            String get_doneIdx_query = "SELECT last_insert_id()";
            doneListIdx.add(this.jdbcTemplate.queryForObject(get_doneIdx_query, int.class)); // doneIdxList에 해당 doneIdx 저장
        }

        return doneListIdx;
    }

}
