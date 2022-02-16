package com.umc.btos.src.letter;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.letter.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class LetterProvider {
    private final LetterDao letterDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public LetterProvider(LetterDao letterDao) {
        this.letterDao = letterDao;
    }

    // ================================================== validation ==================================================

    /*
     * 존재하는 회원인지 확인
     */
    public int checkUserIdx(int userIdx) throws BaseException {
        try {
            return letterDao.checkUserIdx(userIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 존재하는 편지인지 확인
     */
    public int checkLetterIdx(int letterIdx) throws BaseException {
        try {
            return letterDao.checkLetterIdx(letterIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 해당 회원이 작성한 편지인지 확인
     */
    public int checkUserAboutLetter(int userIdx, int letterIdx) throws BaseException {
        try {
            return letterDao.checkUserAboutLetter(userIdx, letterIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================================================

    // 해당 letterIdx를 갖는 Letter 조회
    public GetLetterRes getLetter(int userIdx, int letterIdx) throws BaseException {
        try {
            GetLetterRes getLetterRes = letterDao.getLetter(letterIdx, userIdx);
            // 열람여부 변경 성공 여부 반환 : 성공 시 1, 실패 시 0을 반환
            int isSuccess = letterDao.modifyIsChecked(letterIdx, userIdx);
            if (isSuccess == 0) {
                throw new BaseException(MODIFY_LETTERSENDLIST_ISCHECKED_ERROR);
            }
            return getLetterRes;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
