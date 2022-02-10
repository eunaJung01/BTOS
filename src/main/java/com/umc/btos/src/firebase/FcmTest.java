package com.umc.btos.src.firebase;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.firebase.model.RequestDTO;
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
    public BaseResponse<String> pushMessage(@RequestBody RequestDTO requestDTO) throws IOException, BaseException {
        /*System.out.println(requestDTO.getTargetToken() + " "
        + requestDTO.getTitle() + " " + requestDTO.getBody());*/

        // 디바이스 토큰은 db에 저장해놓고 바뀔 때마다 업데이트해서 가져와야함 -> 테스트 위해 바로 가져와서 진행

        fcmService.sendMessageTo(
                requestDTO.getTo(),
                requestDTO.getProject_id(),
                requestDTO.getNotification()
        );

        //FcmResponse fcmResponse = fcmService.foo();

        return new BaseResponse<>("success");
    }

}
