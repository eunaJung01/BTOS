package com.umc.btos.src.archive;

import com.umc.btos.config.*;
import com.umc.btos.src.archive.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * date = YYYY.MM
     * type (조회 방식) = 1. doneList : 나뭇잎 색으로 done list 개수 표현 / 2. emotion : 감정 이모티콘
     */
    @ResponseBody
    @GetMapping("/calendar/{userIdx}/{date}")
    public BaseResponse<List<GetCalendarRes>> getCalendar(@PathVariable("userIdx") int userIdx, @PathVariable("date") String date, @RequestParam("type") String type) {
        try {
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
     * startDate, lastDate = 날짜 기간 설정 (YYYY.MM.DD ~ YYYY.MM.DD)
     * 검색 & 기간 설정 조회는 중첩됨
     * 최신순 정렬 (diaryDate 기준 내림차순 정렬)
     * 페이징 처리 (무한 스크롤) - 20개씩 조회
     *
     * 1. 전체 조회 - default
     * 2. 문자열 검색 (search)
     * 3. 기간 설정 조회 (startDate ~ endDate)
     * 4. 문자열 검색 & 기간 설정 조회 (search, startDate ~ endDate)
     */
    @ResponseBody
    @GetMapping("/diaryList/{userIdx}/{pageNum}")
    public BaseResponsePaging<List<GetDiaryListRes>> getDiaryList(@PathVariable("userIdx") String userIdx, @PathVariable("pageNum") int pageNum, @RequestParam(required = false) String search, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        try {
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
            GetDiaryRes diary = archiveProvider.getDiary(diaryIdx);
            return new BaseResponse<>(diary);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

}
