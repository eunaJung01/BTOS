package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.Constant;
import com.umc.btos.src.letter.model.*;
import com.umc.btos.src.letter.model.PostLetterRes;

import com.umc.btos.src.plant.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;
import static com.umc.btos.config.BaseResponseStatus.MODIFY_FAIL_LETTER_STATUS;


@Service
public class LetterService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LetterDao letterDao;
    private final LetterProvider letterProvider;
    private final PlantService plantService;

    @Autowired
    public LetterService(LetterDao letterDao, LetterProvider letterProvider,PlantService plantService) {
        this.letterDao = letterDao;
        this.letterProvider = letterProvider;
        this.plantService = plantService;
    }


    // ******************************************************************************
    // 편지 작성(POST)

    public PostLetterRes createLetter(PostLetterReq postLetterReq) throws BaseException {

        try {
            int letterIdx = letterDao.createLetter(postLetterReq); // 편지 생성
            letterDao.createLetterSendList(letterIdx); // 휴먼상태가 아닌 user 5명을 랜덤으로 골라 편지 발송 //letterSendList에 추가
            return new PostLetterRes(letterIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            exception.printStackTrace(); // 에러 발생 원인 출력
            throw new BaseException(DATABASE_ERROR);
        }
    }

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

}
