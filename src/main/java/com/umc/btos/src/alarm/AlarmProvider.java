package com.umc.btos.src.alarm;

import com.umc.btos.config.BaseException;
import com.umc.btos.src.alarm.model.GetAlarmListRes;
import com.umc.btos.src.alarm.model.GetAlarmRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class AlarmProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AlarmDao alarmDao;

    @Autowired
    public AlarmProvider(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    /*
     * 존재하는 회원인지 확인
     */
    public int checkUserIdx(int userIdx) throws BaseException {
        try {
            return alarmDao.checkUserIdx(userIdx); // 존재하면 1, 존재하지 않는다면 0 반환

        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // ================================================================================

    /*
     * 알림 목록 조회
     * [GET] /alarms?userIdx=
     * Alarm.status = 'active'인 알림들만 조회
     * 수신일(createdAt) 기준 내림차순 정렬
     */
    public List<GetAlarmListRes> getAlarmList(int userIdx) throws BaseException, NullPointerException {
        try {
            List<GetAlarmListRes> alarmList;

            // 알림 목록에 띄워줄 알림이 존재하는지 확인 (Alarm.status = 'active')
            if (alarmDao.checkAlarmList(userIdx) == 1) {
                alarmList = new ArrayList<>(alarmDao.getAlarmList(userIdx));

            } else {
                throw new NullPointerException(); // 해당 회원에게 띄워줄 알림이 없습니다.
            }

            return alarmList;

        } catch (NullPointerException exception) {
            throw new BaseException(NO_ALARM); // 해당 회원에게 띄워줄 알림이 없습니다.
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /*
     * 알림 조회
     * [GET] /alarms/:alarmIdx?userIdx=&type=&typeIdx=
     */
//    public class GetAlarmRes {
//        private String alarmType;
//        private int reqParamIdx;
//        /*
//         * alarmType = diary / letter / reply -> reqParamIdx = userIdx (우편함 목록 조회)
//         * alarmType = plant -> reqParamIdx = uPlantIdx (해당 화분 조회)
//         * alarmType = report -> reqParamIdx = diaryIdx, letterIdx, replyIdx (신고 당한 일기/편지/답장 조회)
//         */
//    }
//    public GetAlarmRes getAlarm(int alarmIdx, int userIdx, String type, int typeIdx) throws BaseException {
//        try {
//            String alarmType = type; // diary, letter, reply, plant, report_diary, report_letter, report_reply, notice
//            int reqParamIdx = 0; // 알림 클릭 시 해당 화면으로 전환되기 위해 필요한 parameter 값
//            /*
//             * alarmType = diary / letter / reply -> reqParamIdx = userIdx (우편함 목록 조회)
//             * alarmType = plant -> reqParamIdx = uPlantIdx (해당 화분 조회)
//             * alarmType = report_diary -> reqParamIdx = diaryIdx, letterIdx, replyIdx (신고 당한 일기 조회)
//             * alarmType = report_letter -> reqParamIdx = letterIdx (신고 당한 편지 조회)
//             * alarmType = report_reply -> reqParamIdx = replyIdx (신고 당한 답장 조회)
//             * alarmType = notice -> reqParamIdx = noticeIdx (해당 공지사항 조회)
//             */
//
//            // type 종류 : diary, letter, reply, plant, report, notice
//            switch (type) {
//                case "diary" :
//                    // userIdx 가져오기
//
//                    break;
//
//                case "letter" :
//                    // userIdx 가져오기
//                    break;
//
//                case "reply" :
//                    // userIdx 가져오기
//                    break;
//
//                case "plant" :
//                    // uPlantIdx 가져오기
//                    break;
//
//                case "report" :
//                    // Alarm.type == diary인 경우
//                    // diaryIdx 가져오기
//
//                    // Alarm.type == letter인 경우
//                    // diaryIdx 가져오기
//
//                    // Alarm.type == reply인 경우
//                    // diaryIdx 가져오기
//
//                    break;
//
//                case "notice" :
//                    // noticeIdx 가져오기
//                    break;
//            }
//
//            return new GetAlarmRes(alarmType, reqParamIdx);
//
//        } catch (Exception exception) {
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

}
