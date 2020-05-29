package com.citron.javaintegrationsalesforce.model;

import javax.persistence.*;

@Entity
@Table(name = "api_connect")
public class ApiConnect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "access_token")
    private String token;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "expired")
    private Boolean expired;

    public ApiConnect(){}

    public ApiConnect(String token, String refreshToken, Boolean expired){
        this.token = token;
        this.refreshToken = refreshToken;
        this.expired = expired;
    }

    public void setId(Long id) { this.id = id; }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

}
