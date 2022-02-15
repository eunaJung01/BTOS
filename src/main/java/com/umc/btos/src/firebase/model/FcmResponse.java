package com.umc.btos.src.firebase.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class FcmResponse {
    private String title;
    private String body;
    private String android_channel_id;
    private String channel_id;

}
