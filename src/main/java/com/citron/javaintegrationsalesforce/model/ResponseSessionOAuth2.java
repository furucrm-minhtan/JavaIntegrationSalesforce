package com.citron.javaintegrationsalesforce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseSessionOAuth2 {

    @JsonProperty("id")
    private String id;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("access_token")
    private String token;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("instance_url")
    private String instanceUrl;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("issued_at")
    private String issuedAt;

    @JsonProperty("message")
    private String message;

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

    public String getMessage(){
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
