package com.umc.btos.src.archive;

import com.umc.btos.config.*;
import com.umc.btos.src.archive.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/archives")
public class ArchiveController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ArchiveProvider archiveProvider;

    public ArchiveController(ArchiveProvider archiveProvider) {
        this.archiveProvider = archiveProvider;
    }

    /*
     * 달력 조회
     * [GET] /archives/calendar/:userIdx/:date?type=
     * date = yyyy.MM
     * type (조회 방식) = 1. doneList : 나뭇잎 색으로 done list 개수 표현 / 2. emotion : 감정 이모티콘
     */
    @ResponseBody
    @GetMapping("/calendar/{userIdx}/{date}")
    public BaseResponse<List<GetCalendarRes>> getCalendar(@PathVariable("userIdx") int userIdx, @PathVariable("date") String date, @RequestParam("type") String type) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? & User.status = 'active'
            if (archiveProvider.checkUserIdx(userIdx) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않거나 탈퇴한 회원입니다.
            }

            List<GetCalendarRes> calendar = archiveProvider.getCalendar(userIdx, date, type);
            return new BaseResponse<>(calendar);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /*
     * 일기 리스트 조회
     * [GET] /archives/diaryList/:userIdx/:pageNum?search=&startDate=&endDate=
     * search = 검색할 문자열 ("String")
     * startDate, lastDate = 날짜 기간 설정 (yyyy.MM.dd ~ yyyy.MM.dd)
     * 검색 & 기간 설정 조회는 중첩됨
     * 검색 시 띄어쓰기, 영문 대소문자 구분없이 조회됨
     * 최신순 정렬 (diaryDate 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     */
    @ResponseBody
    @GetMapping("/diaryList/{userIdx}/{pageNum}")
    public BaseResponsePaging<List<GetDiaryListRes>> getDiaryList(@PathVariable("userIdx") String userIdx, @PathVariable("pageNum") int pageNum, @RequestParam(defaultValue = "") String search, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        try {
            // TODO : 형식적 validation - 존재하는 회원인가? & User.status = 'active' / pageNum == 0인 경우
            if (archiveProvider.checkUserIdx(Integer.parseInt(userIdx)) == 0) {
                throw new BaseException(INVALID_USERIDX); // 존재하지 않거나 탈퇴한 회원입니다.
            }
            if (pageNum == 0) {
                throw new BaseException(PAGENUM_ERROR_0); // 페이지 번호는 1부터 시작합니다.
            }

            search = search.replaceAll("\"", ""); // 따옴표 제거
            String[] params = new String[]{userIdx, search, startDate, endDate};
            PagingRes pageInfo = new PagingRes(pageNum, Constant.DIARYLIST_DATA_NUM); // 페이징 정보

            List<GetDiaryListRes> diaryList = archiveProvider.getDiaryList(params, pageInfo);
            return new BaseResponsePaging<>(diaryList, pageInfo);

        } catch (BaseException exception) {
            return new BaseResponsePaging<>(exception.getStatus());
        }
    }

    /*
     * 일기 조회
     * [GET] /archives/:diaryIdx
     */
    @ResponseBody
    @GetMapping("/{diaryIdx}")
    public BaseResponse<GetDiaryRes> getDiary(@PathVariable("diaryIdx") int diaryIdx) {
        try {
            // TODO : 형식적 validation - 존재하는 일기인가?
            if (archiveProvider.checkDiaryIdx(diaryIdx) == 0) {
                throw new BaseException(INVALID_DIARYIDX); // 존재하지 않는 일기입니다.
            }

            GetDiaryRes diary = archiveProvider.getDiary(diaryIdx);
            return new BaseResponse<>(diary);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
