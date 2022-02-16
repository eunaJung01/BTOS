package com.umc.btos.src.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.umc.btos.src.firebase.model.*;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
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
public class FirebaseCloudMessageService {

    // HTTP v1 Method
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/btos-7c7ee/messages:send";
    private final ObjectMapper objectMapper;

    public FcmResponse sendMessageTo(FcmRequest fcmRequest) throws IOException {
        String message = makeMessage(fcmRequest.getToken(),
                fcmRequest.getTitle(),
                fcmRequest.getBody());

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request)
                .execute();

        /*System.out.println("code " + response.code() +
                "body : "+ response.body().string());*/

        return new FcmResponse(
                fcmRequest.getTitle(),
                fcmRequest.getBody(),
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
                    .image(null)
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
