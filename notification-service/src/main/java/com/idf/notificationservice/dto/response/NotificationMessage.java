package com.idf.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationMessage {
    private String email;
    private String subject;
    private String body;

}
