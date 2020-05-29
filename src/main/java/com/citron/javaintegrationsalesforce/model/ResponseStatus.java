package com.citron.javaintegrationsalesforce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseStatus {
    @JsonProperty("id")
    private String id;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("access_token")
    private String access_token;

    @JsonProperty("message")
    private String message;

    public ResponseStatus() {
    }

    public ResponseStatus(boolean success, int statusCode, String access_token, String message) {
        this.success = success;
        this.statusCode = statusCode;
        this.access_token = access_token;
        this.message = message;
    }

    public String getId(){
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getSuccess(){
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getAccessToken(){
        return access_token;
    }

    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
