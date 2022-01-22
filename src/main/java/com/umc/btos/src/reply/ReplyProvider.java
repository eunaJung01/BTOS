package com.umc.btos.src.reply;

import com.umc.btos.src.letter.LetterDao;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReplyProvider {
    private final ReplyDao replyDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public ReplyProvider(ReplyDao replyDao, JwtService jwtService) {
        this.replyDao = replyDao;
        this.jwtService = jwtService;
    }

}
