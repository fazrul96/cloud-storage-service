package com.cloud.storage_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {
    private String userId;
    private String prefix;
    private String fileName;
    private String language = "in_ID";
    private String channel = "web";
    private String requestId = UUID.randomUUID().toString();
}