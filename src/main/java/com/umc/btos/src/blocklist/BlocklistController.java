package com.umc.btos.src.blocklist;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.blocklist.model.*;
import com.umc.btos.src.blocklist.BlocklistProvider;
import com.umc.btos.src.blocklist.BlocklistService;

import com.umc.btos.src.letter.model.PatchLetterReq;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.GET_FAIL_USERIDX;

@RestController
@RequestMapping("/btos/blocklists")
public class BlocklistController {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기
    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    private final BlocklistProvider blocklistProvider;
    @Autowired
    private final BlocklistService blocklistService;
    @Autowired
    private final JwtService jwtService;

    public BlocklistController(BlocklistProvider blocklistProvider, BlocklistService blocklistService, JwtService jwtService) {
        this.blocklistProvider = blocklistProvider;
        this.blocklistService = blocklistService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }
    /**
            * 차단 작성 API
     * [POST] /btos/blocklists
     */
    // Body
    @ResponseBody
    @PostMapping("")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostBlocklistRes> createBlocklist(@RequestBody PostBlocklistReq postBlocklistReq) {
        //  @RequestBody란, 클라이언트가 전송하는 HTTP Request Body(우리는 JSON으로 통신하니, 이 경우 body는 JSON)를 자바 객체로 매핑시켜주는 어노테이션
        try{
            PostBlocklistRes postBlocklistRes = blocklistService.createBlocklist(postBlocklistReq);
            return new BaseResponse<>(postBlocklistRes);
        } catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }
    /**
     * 차단 해제 API
     * [PATCH] /btos/blocklists/:blockIdx
     */
    @ResponseBody
    @PatchMapping("/{blockIdx}")
    // Path-variable
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
     * [GET] /btos/blocklists?userIdx=
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
