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

    // 답장 작성(POST)
    public PostReplyFinalRes createReply(PostReplyReq postReplyReq) throws BaseException {

        try {
            int replyIdx = replyDao.createReply(postReplyReq);
            PostReplyFinalRes postReplyFinalRes = getReplyreceiverNickname(replyIdx, postReplyReq); // 받는 유저의 userIdx, 답장을 보내는 사람의 닉네임 반환

            alarmService.postAlarm_reply(postReplyFinalRes.getReplyIdx(), postReplyFinalRes.getSenderNickName(), postReplyFinalRes.getReceiverIdx()); // 알림 저장
            return postReplyFinalRes;

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지 : 8004

            throw new BaseException(REPLY_DATABASE_ERROR);
        }
    }

    // 답장 작성자의 정보 반환
    // POST API - nickName, ReceiverIdx를 PostReplyFinalRes 객체를 만들어 반환
    public PostReplyFinalRes getReplyreceiverNickname(int replyIdx, PostReplyReq postReplyReq) throws BaseException {

        try {
            // 답장 발송 유저의 닉네임
            String senderNickName = replyDao.getNickname(postReplyReq.getReplierIdx());
            PostReplyFinalRes postReplyFinalRes = new PostReplyFinalRes(replyIdx, postReplyReq.getReceiverIdx(), senderNickName);
            return postReplyFinalRes;

        } catch (Exception exception) {
            // DB에 이상이 있는 경우 에러 메시지 : 8004
            throw new BaseException(REPLY_DATABASE_ERROR);
        }
    }

    // 답장삭제 - status를 deleted로 변경
    public void modifyReplyStatus(PatchReplyReq patchReplyReq) throws BaseException {
        try {
            // Success = 1,  Fail = 0
            int result = replyDao.modifyReplyStatus(patchReplyReq);
            if (result == 0) {
                //ERROR - 8005 : 답장 삭제 실패
                throw new BaseException(MODIFY_FAIL_REPLY_STATUS);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
