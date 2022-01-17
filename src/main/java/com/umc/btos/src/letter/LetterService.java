package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.letter.model.*;
import com.umc.btos.src.letter.model.PostLetterRes;
import com.umc.btos.src.report.model.PostReportRes;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class LetterService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final LetterDao letterDao;
    private final LetterProvider letterProvider;
    private final JwtService jwtService;

    @Autowired //readme 참고
    public LetterService(LetterDao letterDao, LetterProvider letterProvider, JwtService jwtService) {
        this.letterDao = letterDao;
        this.letterProvider = letterProvider;
        this.jwtService = jwtService;

    }

    // ******************************************************************************
    // 편지 작성(POST)

    public PostLetterRes createLetter(PostLetterReq postLetterReq) throws BaseException {

        try {
            int letterIdx = letterDao.createLetter(postLetterReq);
            return new PostLetterRes(letterIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.

            throw new BaseException(DATABASE_ERROR);
        }
    }





}
