package com.umc.btos.src.firebase;

import com.umc.btos.config.*;
import com.umc.btos.src.firebase.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/fcm")
public class FcmTest {

    @Autowired
    private final FirebaseCloudMessageService fcmService;

    public FcmTest(FirebaseCloudMessageService fcmService) {
        this.fcmService = fcmService;
    }

    @ResponseBody
    @PostMapping("")
    public BaseResponse<FcmResponse> pushMessage(@RequestBody FcmRequest fcmRequest) throws IOException, BaseException {

        FcmResponse fcmResponse = fcmService.sendMessageTo(fcmRequest);

        return new BaseResponse<>(fcmResponse);
    }

}
