package com.umc.btos.src.blocklist;
import com.umc.btos.config.BaseException;
import com.umc.btos.src.blocklist.model.*;

import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;


@Service
public class BlocklistService {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final BlocklistDao blocklistDao;
    private final BlocklistProvider blocklistProvider;
    private final JwtService jwtService;


    @Autowired //readme 참고
    public BlocklistService(BlocklistDao blocklistDao, BlocklistProvider blocklistProvider, JwtService jwtService) {
        this.blocklistDao = blocklistDao;
        this.blocklistProvider = blocklistProvider;
        this.jwtService = jwtService;

    }
    // ******************************************************************************
    // 차단 작성(POST)

    public PostBlocklistRes createBlocklist(PostBlocklistReq postBlocklistReq) throws BaseException {

        try {
            int blockIdx = blocklistDao.createBlocklist(postBlocklistReq);
            return new PostBlocklistRes(blockIdx);

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.

            throw new BaseException(DATABASE_ERROR);
        }
    }


}
