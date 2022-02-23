package com.umc.btos.src.suggest;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.suggest.model.PostSuggestReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class SuggestService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SuggestDao suggestDao;

    @Autowired
    public SuggestService(SuggestDao suggestDao) {
        this.suggestDao = suggestDao;
    }

    /*
     * 건의 저장
     * [POST] /suggests
     */
    public int postSuggest(PostSuggestReq postSuggestReq) throws BaseException {
        try {
            int suggestIdx = suggestDao.postSuggest(postSuggestReq);
            return suggestIdx;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
