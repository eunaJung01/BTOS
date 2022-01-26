package com.umc.btos.src.blocklist;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.blocklist.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.GET_FAIL_USERIDX;

@RestController
@RequestMapping("/blocklists")
public class BlocklistController {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기
    @Autowired
    private final BlocklistProvider blocklistProvider;
    @Autowired
    private final BlocklistService blocklistService;


    public BlocklistController(BlocklistProvider blocklistProvider, BlocklistService blocklistService) {
        this.blocklistProvider = blocklistProvider;
        this.blocklistService = blocklistService;
    }
    /**
            * 차단 작성 API
     * [POST] /blocklists
     */
    // Body에 json 파일을 담아 차단 데이터 생성
    @ResponseBody
    @PostMapping("")    // POST 어노테이션
    public BaseResponse<PostBlocklistRes> createBlocklist(@RequestBody PostBlocklistReq postBlocklistReq) {

        try{
            PostBlocklistRes postBlocklistRes = blocklistService.createBlocklist(postBlocklistReq);
            return new BaseResponse<>(postBlocklistRes);
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }
    /**
     * 차단 해제 API
     * [PATCH] /blocklists/:blockIdx
     */
    @ResponseBody
    @PatchMapping("/{blockIdx}")
    // Path-variable - blockIdx를 path-variable로 입력받아 차단의 status를 deleted로 변경
    public BaseResponse<String> modifyBlock(@PathVariable("blockIdx") int blockIdx) {
        try {
            PatchBlocklistReq patchBlocklistReq = new PatchBlocklistReq(blockIdx);
            blocklistService.modifyBlockStatus(patchBlocklistReq);
            String result = "차단이 해제되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
    /**
     * 차단목록조회 API
     * [GET] /blocklists?userIdx=
     *
     * Query String : userIdx
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetBlocklistRes>> getBlockList(@RequestParam(required = false, defaultValue = "-1") int userIdx) {
        // @RequestParam을 통해 파라미터를 입력받는다.
        // required, defaultValue를 통해 userIdx값이 주어지지않았을 때 디폴트값으로 -1로 설정
        try {
            if (userIdx == -1) { // query string인 userIdx이 없을 경우, 에러를 방생시킨다.
                return new BaseResponse<>(GET_FAIL_USERIDX);
            }
            //조회 성공 시 : List<GetBlocklistRes> 형태로 결과(차단목록) 반환
            //DATABASE_ERROR : "데이터베이스 연결에 실패하였습니다." - 4000
            List<GetBlocklistRes> getBlocklistRes = blocklistProvider.getBlockList(userIdx); //조회(read) -> Provider
            return new BaseResponse<>(getBlocklistRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
