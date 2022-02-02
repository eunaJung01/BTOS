package com.umc.btos.src.diary.model;

import com.umc.btos.src.plant.model.PatchModifyScoreRes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PostDiaryRes {
//    private List<UserIdx> diarySendList;
    private PatchModifyScoreRes plantRes;
}
