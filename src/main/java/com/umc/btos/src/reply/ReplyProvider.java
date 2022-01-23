package com.umc.btos.src.reply;

import com.umc.btos.config.BaseException;


import com.umc.btos.src.reply.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class ReplyProvider {
    private final ReplyDao replyDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public ReplyProvider(ReplyDao replyDao, JwtService jwtService) {
        this.replyDao = replyDao;
        this.jwtService = jwtService;
    }
    // 해당 letterIdx를 갖는 Letter의 정보 조회
    public GetReplyRes getReply(int replyIdx) throws BaseException {
        try {
            GetReplyRes getReplyRes = replyDao.getReply(replyIdx);
            return getReplyRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
