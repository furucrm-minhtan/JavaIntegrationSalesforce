package com.citron.javaintegrationsalesforce;

import com.citron.javaintegrationsalesforce.config.ConnectSalesforceConfig;
import com.citron.javaintegrationsalesforce.model.ApiConnect;
import com.citron.javaintegrationsalesforce.model.ResponseSessionOAuth2;
import com.citron.javaintegrationsalesforce.model.ResponseStatus;
import com.citron.javaintegrationsalesforce.repository.ApiConnectRepository;
import com.citron.javaintegrationsalesforce.services.AuthServiceImp;
import com.citron.javaintegrationsalesforce.util.ConnectSalesforce;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class AuthServiceImpTest {

    private MockRestServiceServer mockServer;

    private AuthServiceImp authServiceImp;

    @Mock
    private ApiConnectRepository apiConnectRepository;

    @Mock
    private ConnectSalesforce connectSF;

    @Mock
    private ConnectSalesforceConfig config;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        authServiceImp = new AuthServiceImp(apiConnectRepository, connectSF, config, restTemplate);
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void whenCheckConnect_thenSetExpiredTrueIfTokenExpired() {
        List<ApiConnect> list = new ArrayList<ApiConnect>();
        ApiConnect connect = new ApiConnect("test", "test", false);
        list.add(connect);
        given(apiConnectRepository.findAll()).willReturn(list);
        ConnectSalesforceConfig testConfig = new ConnectSalesforceConfig();
        testConfig.setAppUri("https://eap-prototype-dev-ed.my.salesforce.com/services/data/v48.0/sobjects");
        given(config.getAppUri()).willReturn(testConfig.getAppUri());
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setId("test");
        responseStatus.setSuccess(true);
        responseStatus.setStatusCode(200);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Test");
        given(restTemplate.exchange(testConfig.getAppUri(), HttpMethod.GET, new HttpEntity<String>(null, headers), ResponseStatus.class))
                .willReturn(new ResponseEntity(responseStatus, HttpStatus.UNAUTHORIZED));
        ApiConnect testConnect = authServiceImp.checkConnectSalesforce();
        connect.setExpired(true);
        assertThat(testConnect).isEqualTo(connect);
    }

    @Test
    public void whenCheckConnect_thenReturnEmptyIfTokenNotExist() {
        List<ApiConnect> list = new ArrayList<ApiConnect>();
        ApiConnect connect = new ApiConnect();
        list.add(connect);
        given(apiConnectRepository.findAll()).willReturn(list);
        ApiConnect testConnect = authServiceImp.checkConnectSalesforce();
        assertThat(testConnect).isEqualTo(connect);
    }

    @Test
    public void checkAuthen_ExpiredFalse() {
        String urlAuth = "Test";
        List<ApiConnect> list = new ArrayList<ApiConnect>();
        ApiConnect connect = new ApiConnect("test", "test", true);
        list.add(connect);
        given(apiConnectRepository.findAll()).willReturn(list);
        given(connectSF.getCode()).willReturn(urlAuth);
        RedirectView redirectView = authServiceImp.authenSalesforce();
        assertThat(redirectView.getUrl()).isEqualTo(urlAuth);
    }

    @Test
    public void checkAuthen_ExpiredTrue() {
        List<ApiConnect> list = new ArrayList<ApiConnect>();
        ApiConnect connect = new ApiConnect("test", "test", false);
        list.add(connect);
        given(apiConnectRepository.findAll()).willReturn(list);
        RedirectView redirectView = authServiceImp.authenSalesforce();
        assertThat(redirectView.getUrl()).isEqualTo("/profile");
    }

    @Test
    public void checkAuthen_NoToken() {
        String urlAuth = "Test";
        List<ApiConnect> list = new ArrayList<ApiConnect>();
        given(apiConnectRepository.findAll()).willReturn(list);
        given(connectSF.getCode()).willReturn(urlAuth);
        RedirectView redirectView = authServiceImp.authenSalesforce();
        assertThat(redirectView.getUrl()).isEqualTo(urlAuth);
    }

    @Test
    public void checkGetTokenSuccess() {
        String code = "Test";
        ResponseSessionOAuth2 reponseTestToken = new ResponseSessionOAuth2();
        reponseTestToken.setSuccess(true);
        reponseTestToken.setToken("Test");
        given(connectSF.getToken(code)).willReturn(reponseTestToken);
        given(apiConnectRepository.save(any(ApiConnect.class))).willReturn(new ApiConnect("test", "test", false));
        authServiceImp.getTokenSalesforce(code);
    }
}
