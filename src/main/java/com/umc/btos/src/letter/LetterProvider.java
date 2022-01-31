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

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public LetterProvider(LetterDao letterDao) {

        this.letterDao = letterDao;

    }

    // 해당 letterIdx를 갖는 Letter 조회
    public GetLetterRes getLetter(int letterIdx) throws BaseException {
        try {
            GetLetterRes getLetterRes = letterDao.getLetter(letterIdx);
            return getLetterRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
