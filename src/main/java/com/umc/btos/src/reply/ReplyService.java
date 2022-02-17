package com.umc.btos.src.reply;

import com.umc.btos.config.BaseException;

import com.umc.btos.src.alarm.AlarmService;
import com.umc.btos.src.reply.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class ReplyService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ReplyDao replyDao;
    private final AlarmService alarmService;

    @Autowired
    public ReplyService(ReplyDao replyDao, AlarmService alarmService) {
        this.replyDao = replyDao;
        this.alarmService = alarmService;
    }

    // ================================================================================================================

    /*
     * 답장 저장 및 발송
     * [POST] /replies
     */
    public int postReply(PostReplyReq postReplyReq) throws BaseException {
        try {
            int replierIdx = postReplyReq.getReplierIdx(); // 발신인 userIdx
            int receiverIdx = postReplyReq.getReceiverIdx(); // 수신인 userIdx

            // 답장 저장
            int replyIdx = replyDao.postReply(postReplyReq);
            String senderNickName = replyDao.getNickName(replierIdx); // 발신인 닉네임

            // 알림 저장
            alarmService.postAlarm_reply(replyIdx, senderNickName, receiverIdx);

            return replyIdx;

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================================================

    /*
     * 답장 삭제
     * [PATCH] /replies/:replyIdx?userIdx=
     */
    public void deleteReply(int replyIdx) throws BaseException {
        if (replyDao.deleteReply(replyIdx) == 0) {
            throw new BaseException(MODIFY_FAIL_REPLY_STATUS); // 답장 삭제에 실패하였습니다.
        }
    }

}
