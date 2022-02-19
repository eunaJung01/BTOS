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

    // ================================================== validation ==================================================

    /*
     * 존재하는 회원인지 확인
     */
    public int checkUserIdx(int userIdx) throws BaseException {
        try {
            return replyDao.checkUserIdx(userIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 존재하는 답장인지 확인
     */
    public int checkReplyIdx(int letterIdx) throws BaseException {
        try {
            return replyDao.checkReplyIdx(letterIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 해당 회원이 작성한 답장인지 확인
     */
    public int checkUserAboutReply(int userIdx, int letterIdx) throws BaseException {
        try {
            return replyDao.checkUserAboutReply(userIdx, letterIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================================================

    /*
     * 답장 조회
     * [GET] /replies/:replyIdx?userIdx=
     * 답장 열람 여부 변경 (Reply.isChecked : 0 -> 1)
     */
//    public GetReplyRes getReply(int replyIdx) throws BaseException {
//        try {
//            GetReplyRes getReplyRes = replyDao.getReply(replyIdx);
//
//            if (replyDao.modifyIsChecked(replyIdx) == 0) { // Reply.isChecked : 0 -> 1
//                throw new BaseException(MODIFY_FAIL_ISCHECKED);
//            }
//            return getReplyRes;
//
//        } catch (BaseException exception) {
//            throw new BaseException(MODIFY_FAIL_ISCHECKED); // 열람 여부 변경에 실패하였습니다.
//        } catch (Exception exception) {
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

}
