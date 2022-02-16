package com.umc.btos.src.reply;

import com.umc.btos.config.BaseException;

import com.umc.btos.src.reply.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class ReplyProvider {

    private final ReplyDao replyDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ReplyProvider(ReplyDao replyDao) {
        this.replyDao = replyDao;
    }

    // 해당 replyIdx를 갖는 Reply 조회
    // 열람 여부 (ischecked 변경) 변경
    public GetReplyRes getReply(int replyIdx) throws BaseException {
        try {
            // 열람여부 변경 성공 여부 반환 // 성공 시 1, 실패 시 0을 반환
            int isSuccess = replyDao.modifyIsChecked(replyIdx);
            if (isSuccess == 0) {
                throw new BaseException(MODIFY_REPLY_ISCHECKED_ERROR);
            }
            // 답장 조회
            GetReplyRes getReplyRes = replyDao.getReply(replyIdx);
            return getReplyRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
