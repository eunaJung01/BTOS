package com.umc.btos.src.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.umc.btos.src.firebase.model.FcmMessage;
import com.umc.btos.src.firebase.model.FcmRequest;
import com.umc.btos.src.firebase.model.FcmResponse;
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

    /*private final String API_URL
            = "https://fcm.googleapis.com/fcm/send";

    public FcmResponse sendMessageTo(FcmRequest fcmRequest) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();

        okhttp3.RequestBody requestBody
                = new FormBody.Builder()
                .add("to", fcmRequest.getToken())
                .add("project_id", fcmRequest.getTitle())
                .add("notification", fcmRequest.getBody())
                .add("data", "람쥐썬더")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader(HttpHeaders.AUTHORIZATION, "key=" + getAccessToken()) // server key
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        System.out.println("code : "+ response.code() + "\n" + response.body().string());

        return new FcmResponse(
                fcmRequest.getTitle(),
                fcmRequest.getBody(),
                "LikeFirst_BTOS",
                "LikeFirst_BTOS");
    }


    private String getAccessToken() throws IOException {
        //String firebaseConfigPath = "firebase/firebase_service_key.json";

        String token =
                "AAAA4ageO_g:APA91bGMNBpRuyibDVwa9RptTRXZb5wFmMXk1Z9VE_tVyb0zl5re63CGQUgudTuXBaJnXTQzP__2m-YwSQ7Mefca20Fo_OWaRK23NMGKUQhGtwtP3kblibvqHapWqlYaptwhYXIqqhBw";
        // TODO : 서버 키 Secret.java 파일에 숨기고 가져오는 걸로 변경하기
        return token;
    }*/

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

        System.out.println("code" + response.code() +
                "body : "+ response.body().string());

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
