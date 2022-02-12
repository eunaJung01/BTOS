package com.umc.btos.src.reply;


import com.umc.btos.config.BaseException;


import com.umc.btos.src.reply.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

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

    public int createReply(PostReplyReq postReplyReq) throws BaseException {

        try {
            int replyIdx = replyDao.createReply(postReplyReq);
            return replyIdx;

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지 : 8004

            throw new BaseException(REPLY_DATABASE_ERROR);
        }
    }

    // 답장 작성(POST)

    public PostReplyRes getReplyreceiverNickname(PostReplyReq postReplyReq) throws BaseException {

        try {
            String senderNickName = replyDao.getNickname(postReplyReq.getReplierIdx());
            PostReplyRes postReplyRes = new PostReplyRes(postReplyReq.getReceiverIdx(),senderNickName );
            return postReplyRes;

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지 : 8004

            throw new BaseException(REPLY_DATABASE_ERROR);
        }
    }

    // 답장삭제 - status를 deleted로 변경 (Patch)
    public void modifyReplyStatus(PatchReplyReq patchReplyReq) throws BaseException {
        try {
            int result = replyDao.modifyReplyStatus(patchReplyReq); // 해당 과정이 무사히 수행되면 True(1), 그렇지 않으면 False(0)입니다.
            if (result == 0) { // result값이 0이면 과정이 실패한 것이므로 에러 메서지를 보냅니다.
                throw new BaseException(MODIFY_FAIL_REPLY_STATUS);
            }
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
