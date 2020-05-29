package com.citron.javaintegrationsalesforce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2.setting")
public class ConnectSalesforceConfig {
    private String client_id;
    private String client_secret;
    private String redirect_uri;
    private String uri_login;
    private String app_uri;

    public void setClientId(String client_id) {
        this.client_id = client_id;
    }

    public String getClientId() {
        return client_id;
    }

    public void setClientSecret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getClientSecret() {
        return client_secret;
    }

    public void setRedirectUri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }

    public String getRedirectUri() {
        return redirect_uri;
    }

    public void setUriLogin(String uri_login) {
        this.uri_login = uri_login;
    }

    public String getUriLogin() {
        return uri_login;
    }

    public void setAppUri(String app_login) {
        this.app_uri = app_login;
    }

    public String getAppUri() {
        return app_uri;
    }
}
