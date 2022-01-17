package com.umc.btos.src.letter;

import com.umc.btos.src.report.ReportDao;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LetterProvider {
    private final LetterDao letterDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public LetterProvider(LetterDao letterDao, JwtService jwtService) {
        this.letterDao = letterDao;
        this.jwtService = jwtService;
    }

}
