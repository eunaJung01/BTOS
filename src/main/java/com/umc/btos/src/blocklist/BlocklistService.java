package com.umc.btos.src.blocklist;
import com.umc.btos.config.BaseException;
import com.umc.btos.src.blocklist.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.umc.btos.config.BaseResponseStatus.DATABASE_ERROR;
import static com.umc.btos.config.BaseResponseStatus.MODIFY_FAIL_BLOCK_STATUS;


@Service
public class BlocklistService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BlocklistDao blocklistDao;
    private final BlocklistProvider blocklistProvider;

    @Autowired
    public BlocklistService(BlocklistDao blocklistDao, BlocklistProvider blocklistProvider) {
        this.blocklistDao = blocklistDao;
        this.blocklistProvider = blocklistProvider;

    }
    // ******************************************************************************

    // 차단 작성(POST)
    public PostBlocklistRes createBlocklist(PostBlocklistReq postBlocklistReq) throws BaseException {

        try {
            int blockIdx = blocklistDao.createBlocklist(postBlocklistReq);
            return new PostBlocklistRes(blockIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 차단 해제 - status를 deleted로 변경 (Patch)
    public void modifyBlockStatus(PatchBlocklistReq patchBlocklistReq) throws BaseException {
        try {
            int result = blocklistDao.modifyBlockStatus(patchBlocklistReq);
            // result값이 0이면 과정이 실패한 것이므로 에러 메서지 : 8002 - 차단 해제 실패
            if (result == 0) {
                throw new BaseException(MODIFY_FAIL_BLOCK_STATUS);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
