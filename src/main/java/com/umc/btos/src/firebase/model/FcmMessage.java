package com.umc.btos.src.firebase.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
// FCM Push Server에 요청할 모델
public class FcmMessage {
    private boolean validate_only;
    private Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Notification notification;
        private String token;
        private FcmData data;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class FcmData {
        private String title;
        private String body;
    }
}
