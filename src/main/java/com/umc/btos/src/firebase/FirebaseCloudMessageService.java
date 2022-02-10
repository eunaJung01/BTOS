package com.umc.btos.src.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor
@Service
public class FirebaseCloudMessageService {
    public static final String apiKey = "AAAA4ageO_g:APA91bGMNBpRuyibDVwa9RptTRXZb5wFmMXk1Z9VE_tVyb0zl5re63CGQUgudTuXBaJnXTQzP__2m-YwSQ7Mefca20Fo_OWaRK23NMGKUQhGtwtP3kblibvqHapWqlYaptwhYXIqqhBw";
    public static final String senderId = "969188195320";
    // String deviceToken = "e9GMuSZ_Q7WUGLa0GacSeC:APA91bHzWlP8fBhDnqfNbCBfXAZe3rQWHAb61qn5R2CLAkg_CNtuJnals4--2vRedNXiqBUNrnp9HFkIxMg2bTAXOL_4mXu5TOj1vjrk6YX69_TLBgse3G3weBEpI50N-5RGAPBQc9mx";

    private final String API_URL
            = "https://fcm.googleapis.com/fcm/send";
            //= "https://fcm.googleapis.com/v1/projects/btos-7c7ee/messages:send";
    private final ObjectMapper objectMapper;

    public void sendMessageTo(String to, String project_id, String notification) throws IOException {
        // String message = makeMessage(targetToken, title, body);
        //targetToken : device 토큰, title
        OkHttpClient client = new OkHttpClient.Builder().build();

        okhttp3.RequestBody requestBody
                //= RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
                = new FormBody.Builder()
                .add("to", to)
                .add("project_id",project_id)
                .add("notification", notification)
                .add("data", "람쥐썬더")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader(HttpHeaders.AUTHORIZATION, "key=" + getAccessToken()) // server key
                //.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .post(requestBody)
                .build();

        /*client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage() + "\n     ERROR");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println(response.code() + "\n" + response.body().string() + "\n     SUCCESS");
                }
                else {
                    System.out.println(response.body().string());
                }
            }
        });*/

        Response response = client.newCall(request).execute();
        System.out.println("code : "+ response.code() + "\n" + response.body().string());
    }

    /*private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
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
    }*/

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/firebase_service_key.json";

        String token =
                "AAAA4ageO_g:APA91bGMNBpRuyibDVwa9RptTRXZb5wFmMXk1Z9VE_tVyb0zl5re63CGQUgudTuXBaJnXTQzP__2m-YwSQ7Mefca20Fo_OWaRK23NMGKUQhGtwtP3kblibvqHapWqlYaptwhYXIqqhBw";

        /*List<String> list = new ArrayList<>();
        list.add("https://www.googleapis.com/auth/cloud-platform"); // 범위 설정

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
                new ClassPathResource(firebaseConfigPath).getInputStream())
                        .createScoped(list);*/

        //googleCredentials.refreshIfExpired();
        //googleCredentials.refreshAccessToken();
        //return googleCredentials.getAccessToken().getTokenValue();
        return token;
    }

}
