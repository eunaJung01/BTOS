package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.letter.model.*;
import com.umc.btos.src.letter.model.PostLetterRes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;
import static com.umc.btos.config.BaseResponseStatus.MODIFY_FAIL_LETTER_STATUS;


@Service
public class LetterService {
    /**
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LetterDao letterDao;
    private final LetterProvider letterProvider;
    private final PlantService plantService;

    @Autowired
    public LetterService(LetterDao letterDao, LetterProvider letterProvider,PlantService plantService) {
        this.letterDao = letterDao;
        this.letterProvider = letterProvider;
        this.plantService  = plantService;
    }


    // ******************************************************************************
    // 편지 작성(POST)
    /**
    public PostLetterRes createLetter(PostLetterReq postLetterReq) throws BaseException {

        try {
            int letterIdx = letterDao.createLetter(postLetterReq);

            // 화분 점수 증가
            PatchUpDownScoreReq patchUpDownScoreReq = new PatchUpDownScoreReq(PLANT_LEVELUP_LETTER); // addscore 는 3점
            plantService.upScore(postLetterReq.getUserIdx(),patchUpDownScoreReq);


            return new PostLetterRes(letterIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.

            throw new BaseException(DATABASE_ERROR);
        }
    }*/
    /**
    // 편지 삭제 - status를 deleted로 변경 (Patch)
    public void modifyLetterStatus(PatchLetterReq patchLetterReq) throws BaseException {
        try {
            int result = letterDao.modifyLetterStatus(patchLetterReq); // 해당 과정이 무사히 수행되면 True(1), 그렇지 않으면 False(0)입니다.
            if (result == 0) { // result값이 0이면 과정이 실패한 것이므로 에러 메서지를 보냅니다.
                throw new BaseException(MODIFY_FAIL_LETTER_STATUS);
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    */

}
