package com.umc.btos.src.notice;

import com.umc.btos.src.blocklist.BlocklistDao;
import com.umc.btos.src.blocklist.BlocklistProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoticeService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NoticeDao noticeDao;
    private final NoticeProvider noticeProvider;


    @Autowired
    public NoticeService(NoticeDao noticeDao, NoticeProvider noticeProvider) {
        this.noticeDao = noticeDao;
        this.noticeProvider = noticeProvider;

    }
}
