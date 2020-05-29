package com.citron.javaintegrationsalesforce.util;

import com.citron.javaintegrationsalesforce.model.ResponseStatus;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class UtilCallApi {

    private String URL_REFRESH_TOKEN = "https://login.salesforce.com/services/oauth2/token";
    private int TIMEOUT = 10000;

    @Value("${oauth2.setting.client_id}")
    private String client_id;
    @Value("${oauth2.setting.client_secret}")
    private String client_secret;
    @Value("${oauth2.setting.redirect_uri}")
    private String redirect_uri;
    @Value("${oauth2.setting.uri_login}")
    private String uri_login;
    @Value("${oauth2.setting.app_uri}")
    private String app_uri;

    public ResponseStatus postRestTemplate(String url, String token, String data) {

        HttpHeaders headers = new HttpHeaders();
        String authHeader = "Bearer " + token;
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity  = new HttpEntity<String>(data, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<ResponseStatus> response = restTemplate.exchange(url, HttpMethod.POST, entity, ResponseStatus.class);
            if(response.getStatusCode() == HttpStatus.CREATED) {
                return response.getBody();
            }

            ResponseStatus resStatus = new ResponseStatus();
            resStatus.setSuccess(false);
            resStatus.setStatusCode(500);
            return resStatus;

        }catch (HttpClientErrorException ex) {

            ResponseStatus resStatus = new ResponseStatus();
            resStatus.setSuccess(false);

            if(ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                resStatus.setStatusCode(401);
            }
            else {
                resStatus.setStatusCode(500);
            }

            return resStatus;
        }
    }

    public ResponseStatus patchRestTemplate(String url, String token, String data) {

        HttpHeaders headers = new HttpHeaders();
        String authHeader = "Bearer " + token;
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<String> entity  = new HttpEntity<String>(data, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);

            restTemplate.setRequestFactory(requestFactory);

            ResponseEntity<ResponseStatus> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, ResponseStatus.class);

            ResponseStatus resStatus = new ResponseStatus();

            if(response.getStatusCode() == HttpStatus.NO_CONTENT) {
                resStatus.setSuccess(true);
                return resStatus;
            }

            resStatus.setSuccess(false);
            resStatus.setStatusCode(500);
            return resStatus;

        }catch (HttpClientErrorException ex) {

            ResponseStatus resStatus = new ResponseStatus();
            resStatus.setSuccess(false);

            if(ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                resStatus.setStatusCode(401);
            }
            else {
                resStatus.setStatusCode(500);
            }

            return resStatus;
        }
    }

    public ResponseStatus deleteRestTemplate(String url, String token) {

        HttpHeaders headers = new HttpHeaders();
        String authHeader = "Bearer " + token;
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<String> entity  = new HttpEntity<String>(null, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {

            ResponseEntity<ResponseStatus> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, ResponseStatus.class);

            ResponseStatus resStatus = new ResponseStatus();

            if(response.getStatusCode() == HttpStatus.NO_CONTENT) {
                resStatus.setSuccess(true);
                return resStatus;
            }

            resStatus.setSuccess(false);
            resStatus.setStatusCode(500);
            return resStatus;

        }catch (HttpClientErrorException ex) {

            ResponseStatus resStatus = new ResponseStatus();
            resStatus.setSuccess(false);

            if(ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                resStatus.setStatusCode(401);
            }
            else {
                resStatus.setStatusCode(500);
            }

            return resStatus;
        }
    }

    public ResponseStatus refreshToken(String code) throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("client_id", client_id);
        map.add("client_secret", client_secret);
        map.add("grant_type", "refresh_token");
        map.add("redirect_uri", redirect_uri);
        map.add("refresh_token", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<ResponseStatus> response = restTemplate.exchange(URL_REFRESH_TOKEN, HttpMethod.POST, request, ResponseStatus.class);
            if(response.getStatusCode() == HttpStatus.OK) {
                ResponseStatus resStatus = response.getBody();
                resStatus.setSuccess(true);
                return resStatus;
            }

            ResponseStatus resStatus = new ResponseStatus();
            resStatus.setSuccess(false);
            return resStatus;

        }catch(HttpClientErrorException ex) {
            ResponseStatus resStatus = new ResponseStatus();
            resStatus.setSuccess(false);
            return resStatus;
        }
    }

    public static LocalDateTime convertDateTimeInsert(String datetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(datetime, formatter).minusHours(9);
        return dateTime;
    }

    public static String convertDateTimeApi(String datetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTimeUTC = LocalDateTime.parse(datetime, formatter).minusHours(9);

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDateTime = dateTimeUTC.format(formatter1);
        return formattedDateTime;
    }
}
