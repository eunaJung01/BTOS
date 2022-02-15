package com.umc.btos.src.firebase;

import com.umc.btos.config.*;
import com.umc.btos.src.firebase.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class FcmTest {
    private final FirebaseCloudMessageService fcmService;


    @PostMapping("/fcm")
    public BaseResponse<FcmResponse> pushMessage(@RequestBody FcmRequest fcmRequest) throws IOException, BaseException {

        FcmResponse fcmResponse = fcmService.sendMessageTo(fcmRequest);

        return new BaseResponse<>(fcmResponse);
    }

}
