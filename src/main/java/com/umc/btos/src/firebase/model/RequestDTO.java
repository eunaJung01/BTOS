package com.umc.btos.src.firebase.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class RequestDTO {
    private String to; // deviceToken
    private String project_id;
    private String notification;

}
