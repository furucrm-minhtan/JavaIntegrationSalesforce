package com.citron.javaintegrationsalesforce.util;

import com.citron.javaintegrationsalesforce.config.ConnectSalesforceConfig;
import com.citron.javaintegrationsalesforce.controller.AuthController;
import com.citron.javaintegrationsalesforce.model.ResponseSessionOAuth2;
import lombok.Data;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Component
public class ConnectSalesforce {
    private final static Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private ConnectSalesforceConfig config;

    public String getCode() {
        return config.getUriLogin()+"/services/oauth2/authorize?client_id="+config.getClientId()+"&redirect_uri="+config.getRedirectUri()+"&response_type=code";
    }

    public ResponseSessionOAuth2 getToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("client_id", config.getClientId());
        map.add("client_secret", config.getClientSecret());
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", config.getRedirectUri());
        map.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<ResponseSessionOAuth2> response = restTemplate.exchange(config.getUriLogin()+"/services/oauth2/token", HttpMethod.POST, request, ResponseSessionOAuth2.class);
            if(response.getStatusCode() == HttpStatus.OK) {
                ResponseSessionOAuth2 resStatus = response.getBody();
                resStatus.setSuccess(true);
                return resStatus;
            }

        }catch(HttpClientErrorException ex) {
            logger.warning(ex.getMessage());
            logger.info(this.getClass().getName()+": "+ex.getResponseBodyAsString());
        }
        ResponseSessionOAuth2 resStatus = new ResponseSessionOAuth2();
        resStatus.setSuccess(false);
        return resStatus;
    }
}
