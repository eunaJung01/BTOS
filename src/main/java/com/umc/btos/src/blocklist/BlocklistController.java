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
}
