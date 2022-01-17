package com.umc.btos.src.diary;

import com.umc.btos.config.*;
import com.umc.btos.src.diary.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/btos/diary")
public class DiaryController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final DiaryProvider diaryProvider;
    @Autowired
    private final DiaryService diaryService;

    public DiaryController(DiaryProvider diaryProvider, DiaryService diaryService) {
        this.diaryProvider = diaryProvider;
        this.diaryService = diaryService;
    }

    /*
     * 일기 저장 : [POST] /btos/diary
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostDiaryRes> saveDiary(@RequestBody PostDiaryReq postDiaryReq) {
        try {
            PostDiaryRes postDiaryRes = diaryService.saveDiary(postDiaryReq);
            return new BaseResponse<>(postDiaryRes);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
