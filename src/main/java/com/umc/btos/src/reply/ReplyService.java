package com.umc.btos.src.reply;


import com.umc.btos.config.BaseException;


import com.umc.btos.src.reply.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;
import static com.umc.btos.config.BaseResponseStatus.REPLY_DATABASE_ERROR;

@Service
public class ReplyService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final ReplyDao replyDao;
    private final ReplyProvider replyProvider;
    private final JwtService jwtService;


    @Autowired //readme 참고
    public ReplyService(ReplyDao replyDao, ReplyProvider replyProvider, JwtService jwtService) {
        this.replyDao = replyDao;
        this.replyProvider = replyProvider;
        this.jwtService = jwtService;

    }

// ******************************************************************************
    // 답장 작성(POST)

    public PostReplyRes createReply(PostReplyReq postReplyReq) throws BaseException {

        try {
            int replyIdx = replyDao.createReply(postReplyReq);
            return new PostReplyRes(replyIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지 : 8004

            throw new BaseException(REPLY_DATABASE_ERROR);
        }
    }



}
