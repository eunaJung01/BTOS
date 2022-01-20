package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.letter.model.*;
import com.umc.btos.src.report.ReportDao;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class LetterProvider {
    private final LetterDao letterDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public LetterProvider(LetterDao letterDao, JwtService jwtService) {
        this.letterDao = letterDao;
        this.jwtService = jwtService;
    }

    // 해당 letterIdx를 갖는 Letter의 정보 조회
    public GetLetterRes getLetter(int letterIdx) throws BaseException {
        try {
            GetLetterRes getLetterRes = letterDao.getLetter(letterIdx);
            return getLetterRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}