package com.umc.btos.src.reply;


import com.umc.btos.src.letter.LetterDao;
import com.umc.btos.src.letter.LetterProvider;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReplyService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final ReplyDao replyDao;
    private final ReplyProvider replyProvider;
    private final JwtService jwtService;


    @Autowired //readme 참고
    public ReplyService(ReplyDao replyDao, ReplyProvider replyProvider, JwtService jwtService) {
        this.replyDao = replyDao;
        this.replyProvider = replyProvider;
        this.jwtService = jwtService;

    }





}
