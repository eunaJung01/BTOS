package com.umc.btos.src.auth;

import com.sun.tracing.dtrace.ProviderAttributes;
import com.umc.btos.config.BaseException;
import com.umc.btos.config.secret.Secret;
import com.umc.btos.src.auth.model.*;
import com.umc.btos.utils.AES128;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import static com.umc.btos.config.BaseResponseStatus.*;

@Service
public class AuthProvider {

    private final AuthDao authDao;
    private final JwtService jwtService;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public AuthProvider(AuthDao authDao, JwtService jwtService) {
        this.authDao = authDao;
        this.jwtService = jwtService;
    }


}
