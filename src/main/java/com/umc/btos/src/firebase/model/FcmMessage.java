package com.umc.btos.src.firebase.model;

import lombok.*;

/*@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)*/

@Builder
@AllArgsConstructor
@Getter
// FCM Server 로 요청할 request model
public class FcmMessage {

    private boolean validate_only;
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
    }


}
