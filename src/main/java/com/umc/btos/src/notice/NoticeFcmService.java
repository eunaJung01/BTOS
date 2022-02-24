package com.umc.btos.src.notice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.umc.btos.src.firebase.model.FcmMessage;
import com.umc.btos.src.firebase.model.FcmResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.HttpHeaders;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Service
public class NoticeFcmService {

    // HTTP v1 Method
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/btos-7c7ee/messages:send";
    private final ObjectMapper objectMapper;
    private final String IMAGE_URL = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/f8e3655e-b372-4971-937f-89a5722483f6/app_icon.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220224%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220224T001544Z&X-Amz-Expires=86400&X-Amz-Signature=c3b7bb368be3c4d3c2b754a8dece7156a596a49a5ae9e425a6dfbe6eb63f3d33&X-Amz-SignedHeaders=host&response-content-disposition=filename%20%3D%22app%2520icon.png%22&x-id=GetObject";

    public FcmResponse sendMessageTo(String targetToken, String title, String body) throws IOException {
        String message = makeMessage(targetToken, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        /*
        Response response = client.newCall(request)
                .execute();

        System.out.println("code " + response.code() +
                "body : "+ response.body().string());*/

        return new FcmResponse(
                title,
                body,
                "LikeFirst_BTOS",
                "LikeFirst_BTOS");
    }

    private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(IMAGE_URL) // Android 1MB 이미지 제한 존재
                                .build()
                        )
                        .build()
                )
                .validate_only(false)
                .build();

        return objectMapper.writeValueAsString(fcmMessage);
    }


    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/firebase_secret_key.json";

        List<String> scopeUrl = new ArrayList<>();
        scopeUrl.add("https://www.googleapis.com/auth/firebase.messaging");

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
                        new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(scopeUrl);

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }


}
