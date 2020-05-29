package com.citron.javaintegrationsalesforce.services;

import com.citron.javaintegrationsalesforce.config.ConnectSalesforceConfig;
import com.citron.javaintegrationsalesforce.model.ApiConnect;
import com.citron.javaintegrationsalesforce.model.ResponseSessionOAuth2;
import com.citron.javaintegrationsalesforce.model.ResponseStatus;
import com.citron.javaintegrationsalesforce.repository.ApiConnectRepository;
import com.citron.javaintegrationsalesforce.util.ConnectSalesforce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.logging.Logger;

@Service
public class AuthServiceImp implements AuthService {
    private final static Logger logger = Logger.getLogger(AuthService.class.getName());
    private RestTemplate restTemplate;
    private ApiConnectRepository repository;
    private ConnectSalesforce connectSF;
    private ConnectSalesforceConfig config;

    @Autowired
    public AuthServiceImp(ApiConnectRepository _repository, ConnectSalesforce _connectSF, ConnectSalesforceConfig _config, RestTemplate _restTemplate) {
        this.repository = _repository;
        this.connectSF =_connectSF;
        this.config = _config;
        this.restTemplate = _restTemplate;
    }

    @Override
    public ApiConnect checkConnectSalesforce() {
        List<ApiConnect> listConnect = repository.findAll();
        ApiConnect connect = null;
        if(!listConnect.isEmpty()) {
            try {
                connect = listConnect.get(0);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer "+connect.getToken());
                ResponseEntity<ResponseStatus> response = restTemplate.exchange(config.getAppUri(), HttpMethod.GET, new HttpEntity<String>(null, headers), ResponseStatus.class);
            }
            catch (HttpClientErrorException ex) {
                logger.warning(ex.getMessage());
                logger.info(this.getClass().getName()+": "+ex.getLocalizedMessage());
                System.out.print(ex.getMessage());
                if( ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    connect.setExpired(true);
                    repository.save(connect);
                }
            }
        }
        return connect;
    }

    @Override
    public RedirectView authenSalesforce() {
        RedirectView redirectView = new RedirectView();
        try {
            List<ApiConnect> connect = repository.findAll();
            if(!connect.isEmpty()) {
                Boolean expired = connect.get(0).getExpired();
                repository.deleteAll();
                if(expired) {
                    redirectView.setUrl(connectSF.getCode());
                }
                else {
                    redirectView.setUrl("/profile");
                }
            }
            else {
                redirectView.setUrl(connectSF.getCode());
            }
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            logger.info(this.getClass().getName()+": "+ex.getLocalizedMessage());
            redirectView.setUrl("/profile");
        }
        return redirectView;
    }

    @Override
    public void getTokenSalesforce(String code) {
        try{
            ResponseSessionOAuth2 reponse = connectSF.getToken(code);
            if(!reponse.getToken().isBlank()) {
                repository.save(new ApiConnect(reponse.getToken(), reponse.getRefreshToken(), false));
            }
        }
        catch(Exception ex) {
            logger.warning(ex.getMessage());
            logger.info(this.getClass().getName()+": "+ex.getLocalizedMessage());
        }
    }
}
