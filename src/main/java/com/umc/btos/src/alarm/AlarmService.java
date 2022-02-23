package com.umc.btos.src.alarm;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.diary.model.GetSendListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class AlarmService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AlarmDao alarmDao;

    @Autowired
    public AlarmService(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    /*
     * Alarm.type = diary
     * 18:59:55 일기 발송 리스트 생성 -> 알림 테이블에 저장
     * 19:00:00 전에 알림 목록 조회 시 isSend = 0인 일기들은 조회되면 안됨
     * -> 알림 목록 조회 시 type = diary인 알림에는 조건 추가
     */
    public void postAlarm_diary(List<GetSendListRes> diarySendList) throws BaseException {
        try {
            for (GetSendListRes diary : diarySendList) {
                for (Integer receiverIdx : diary.getReceiverIdxList()) {
                    String content = "'" + diary.getSenderNickName() + "'에게서 일기가 도착했습니다.";

                    if (alarmDao.postAlarm_diary(receiverIdx, diary.getDiaryIdx(), content) == 0) {
                        throw new BaseException(POST_FAIL_ALARM);
                    }
                }
            }

        } catch (BaseException exception) {
            throw new BaseException(POST_FAIL_ALARM); // 알림 저장에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /*
     * Alarm.type = letter
     * 편지 저장 및 발송 시 편지 발송 리스트 생성 -> 알림 테이블에 저장
     */
    public void postAlarm_letter(int letterIdx, String senderNickName, List<Integer> receiverIdxList) throws BaseException {
        try {
            String content = "'" + senderNickName + "'에게서 편지가 도착했습니다.";

            for (int receiverIdx : receiverIdxList) {
                if (alarmDao.postAlarm_letter(receiverIdx, letterIdx, content) == 0) {
                    throw new BaseException(POST_FAIL_ALARM);
                }
            }

        } catch (BaseException exception) {
            throw new BaseException(POST_FAIL_ALARM); // 알림 저장에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /*
     * Alarm.type = reply
     * 답장 저장 및 발송 시 해당 수신인에 대하여 알림 테이블에 저장
     */
    public void postAlarm_reply(int replyIdx, String senderNickName, int receiverIdx) throws BaseException {
        try {
            String content = "'" + senderNickName + "'에게서 답장이 도착했습니다.";

            if (alarmDao.postAlarm_reply(receiverIdx, replyIdx, content) == 0) {
                throw new BaseException(POST_FAIL_ALARM);
            }


        } catch (BaseException exception) {
            throw new BaseException(POST_FAIL_ALARM); // 알림 저장에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /*
     * Alarm.type = plant
     * 1. 일기 또는 편지 작성 시 '식물 n단계 달성!' -> type = plus
     * 2. 신고 당했을 때 -> type = minus
     *    2-1. 점수 감소 : '신고 처리가 접수되어 화분 점수가 감소하였습니다.'
     *    2-2. 점수 및 단계 감소 : '신고 처리가 접수되어 화분 단계가 n단계로 하락하였습니다.'
     */
    public void postAlarm_plant(String type, int userIdx, int uPlantIdx, int level) throws BaseException {
        try {
            String content = null;
            switch (type) {
                case "plus":
                    content = "화분 " + level + "단계 달성!";
                    break;
                case "minus":
                    if (level == 0) { // 단계가 감소하지 않았을 경우
                        content = "신고 처리가 접수되어 화분 점수가 감소하였습니다.";
                    } else { // 단계도 감소하지 않았을 경우
                        content = "신고 처리가 접수되어 화분 단계가" + level + "단계로 하락하였습니다.";
                    }
                    break;
            }
            if (alarmDao.postAlarm_plant(userIdx, uPlantIdx, content) == 0) {
                throw new BaseException(POST_FAIL_ALARM);
            }

        } catch (BaseException exception) {
            throw new BaseException(POST_FAIL_ALARM); // 알림 저장에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /*
     * Alarm.type = report
     * 신고 접수 시 해당 신고를 당한 회원에 대하여 알림 테이블에 저장
     */
    public void postAlarm_report(int reportIdx, int receiverIdx, String type) throws BaseException {
        try {
            String content = null;

            switch (type) {
                case "diary":
                    content = "귀하의 일기가 신고 접수되었습니다.";
                    break;

                case "letter":
                    content = "귀하의 편지가 신고 접수되었습니다.";
                    break;

                case "reply":
                    content = "귀하의 답장이 신고 접수되었습니다.";
                    break;
            }

            if (alarmDao.postAlarm_report(receiverIdx, reportIdx, content) == 0) {
                throw new BaseException(POST_FAIL_ALARM);
            }

        } catch (BaseException exception) {
            throw new BaseException(POST_FAIL_ALARM); // 알림 저장에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    // type = notice
    public void postAlarm_notice(int noticeIdx, String title) throws BaseException {
        try {
            if (alarmDao.postAlarm_notice(noticeIdx, title) == 0) {
                throw new BaseException(POST_FAIL_ALARM);
            }

        } catch (BaseException exception) {
            throw new BaseException(POST_FAIL_ALARM); // 알림 저장에 실패하였습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
