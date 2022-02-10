package com.umc.btos.src.firebase;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
public class FcmMessage { // FCM Request Body
   /* private boolean validate_only;
    private Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Notification notification;
        private String token;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }*/

    private String to;
    private String project_id;
    private String notification;
}
