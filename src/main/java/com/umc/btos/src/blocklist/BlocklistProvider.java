package com.umc.btos.src.blocklist;


import com.umc.btos.config.BaseException;
import com.umc.btos.src.blocklist.model.GetBlocklistRes;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;

@Service

public class BlocklistProvider {
    private final BlocklistDao blocklistDao;
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public BlocklistProvider(BlocklistDao blocklistDao, JwtService jwtService) {
        this.blocklistDao = blocklistDao;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    //차단 조회 API
    public List<GetBlocklistRes> getBlockList(int userIdx) throws BaseException {
        try {
            List<GetBlocklistRes> getBlocklistRes = blocklistDao.getBlockList(userIdx);
            return getBlocklistRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
