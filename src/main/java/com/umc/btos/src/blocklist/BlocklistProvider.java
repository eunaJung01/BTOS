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


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public BlocklistProvider(BlocklistDao blocklistDao) {
        this.blocklistDao = blocklistDao;
    }

    //차단 조회 API
    // userIdx를 Query String로 받아 차단들을 반환
    public List<GetBlocklistRes> getBlockList(int userIdx) throws BaseException {
        try {
            List<GetBlocklistRes> getBlocklistRes = blocklistDao.getBlockListNickName(userIdx);
            return getBlocklistRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
