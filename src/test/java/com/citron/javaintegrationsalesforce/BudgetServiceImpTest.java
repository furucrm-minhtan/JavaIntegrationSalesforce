package com.citron.javaintegrationsalesforce;

import com.citron.javaintegrationsalesforce.config.ConnectSalesforceConfig;
import com.citron.javaintegrationsalesforce.model.ApiConnect;
import com.citron.javaintegrationsalesforce.model.Budget;
import com.citron.javaintegrationsalesforce.model.ProposalBudget;
import com.citron.javaintegrationsalesforce.model.ResponseStatus;
import com.citron.javaintegrationsalesforce.repository.ApiConnectRepository;
import com.citron.javaintegrationsalesforce.repository.BudgetRepository;
import com.citron.javaintegrationsalesforce.repository.ProposalBudgetRepository;
import com.citron.javaintegrationsalesforce.services.BudgetServiceImp;
import com.citron.javaintegrationsalesforce.util.UtilCallApi;
import com.citron.javaintegrationsalesforce.util.exception.CallApiException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class BudgetServiceImpTest {

    private BudgetServiceImp budgetServiceImp;

    @Mock
    private ConnectSalesforceConfig config;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ApiConnectRepository apiConnectRepository;

    @Mock
    private UtilCallApi utilCallApi;

    @Mock
    private ProposalBudgetRepository proposalBudgetRepository;

    @Before
    public void init() {
        budgetServiceImp = new BudgetServiceImp(apiConnectRepository, budgetRepository, config, proposalBudgetRepository, utilCallApi);
    }

    @Test
    public void  whenCallServiceGetAllBudget_thenReturnListBudget() {
        ArrayList<Budget> list = new ArrayList<>();
        Budget budget1 = new Budget("Test1", "2001");
        Budget budget2 = new Budget("Test2", "2002");
        Budget budget3 = new Budget("Test3", "2003");

        list.add(budget1);
        list.add(budget2);
        list.add(budget3);
        given(budgetRepository.findAll()).willReturn(list);
        List<Budget> resultTest = budgetServiceImp.getAllBudget();
        assertThat(resultTest).isEqualTo(list);
    }

    @Test
    public void whenCallServiceGetBudgetBudgetId_thenReturnBudget() {
        Long id = 1L;
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        given(budgetRepository.findById(id)).willReturn(java.util.Optional.of(budget));
        Budget resultTest = budgetServiceImp.getBudgetById(id);
        assertThat(resultTest).isEqualTo(budget);
    }

    @Test
    public void whenCallServiceGetJunction_thenReturnListJunction() {
        String sfid = "123456";
        List<ProposalBudget> proposalBudgets = new ArrayList<ProposalBudget>();
        ProposalBudget proposalBudget1 = new ProposalBudget();
        proposalBudget1.setName("Test1");
        proposalBudget1.setSfid(sfid);
        ProposalBudget proposalBudget2 = new ProposalBudget();
        proposalBudget2.setName("Test2");
        proposalBudget2.setSfid(sfid);
        ProposalBudget proposalBudget3 = new ProposalBudget();
        proposalBudget3.setName("Test3");
        proposalBudget3.setSfid(sfid);

        proposalBudgets.add(proposalBudget1);
        proposalBudgets.add(proposalBudget2);
        proposalBudgets.add(proposalBudget3);

        given(proposalBudgetRepository.getJunctionFromBudget(sfid)).willReturn(proposalBudgets);

        List<ProposalBudget> resultTest = budgetServiceImp.getJunction(sfid);
        assertThat(resultTest).isEqualTo(proposalBudgets);
    }

    @Test
    public void whenCallServiceGetApiConnect_thenApiConnectRepository() {
        assertThat(budgetServiceImp.getApiConnect()).isEqualTo(apiConnectRepository);
    }

    @Test
    public void whenCreateBudget_returnIdBudgetIfSuccess() throws Exception {
        Long id = 1L;
        String uri = "TestCreatBudget/Budget__c";
        String sfid = "Success";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());
        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);
        responseStatus.setId(sfid);
        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.saveAndFlush(budget)).willReturn(budget);
        given(utilCallApi.postRestTemplate(uri, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);

        Long resultTest = budgetServiceImp.createBudget(budget);
        assertThat(resultTest).isEqualTo(id);
    }

    @Test
    public void whenExpiredTokenCreateBudget_thenRefreshToken() throws Exception {
        Long id = 1L;
        String uri = "TestCreatBudget/Budget__c";
        String sfid = "Success";
        String tokenRefresh = "refresh";
        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(401);
        responseStatus.setSuccess(false);

        ResponseStatus responseStatusRefreshToken = new ResponseStatus();
        responseStatusRefreshToken.setStatusCode(200);
        responseStatusRefreshToken.setSuccess(true);
        responseStatusRefreshToken.setAccessToken(tokenRefresh);

        ResponseStatus responseResultAfterReSynced = new ResponseStatus();
        responseResultAfterReSynced.setStatusCode(200);
        responseResultAfterReSynced.setSuccess(true);
        responseResultAfterReSynced.setId(sfid);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.postRestTemplate(uri, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        given(utilCallApi.refreshToken(apiConnect.getRefreshToken())).willReturn(responseStatusRefreshToken);
        given(utilCallApi.postRestTemplate(uri, tokenRefresh, dataJson.toString())).willReturn(responseResultAfterReSynced);
        given(budgetRepository.saveAndFlush(budget)).willReturn(budget);

        Long resultTest = budgetServiceImp.createBudget(budget);
        assertThat(resultTest).isEqualTo(id);
    }

    @Test
    public void whenNotConnectToSalesforceCreateBudget_thenReturnMessageError() throws Exception {
        String uri = "TestCreatBudget/Budget__c";
        Budget budget = new Budget("Test1", "2001");
        Boolean resultTest = null;

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(null);

        try {
            budgetServiceImp.createBudget(budget);
        }
        catch(Exception ex) {
            assertThat(ex.getMessage().contains("Your application is not connected to salesforce or access token expired")).isEqualTo(true);
            resultTest = false;
        }
        assertThat(resultTest).isEqualTo(false);
    }

    @Test
    public void whenCreateBudget_returnIdIs0IfFail() throws Exception {
        Long id = 1L;
        String uri = "TestCreatBudget/Budget__c";
        String sfid = "Success";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());
        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(500);
        responseStatus.setSuccess(false);
        responseStatus.setId(sfid);
        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.saveAndFlush(budget)).willReturn(budget);
        given(utilCallApi.postRestTemplate(uri, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);

        Long resultTest = budgetServiceImp.createBudget(budget);
        assertThat(resultTest).isEqualTo(0);
    }

    @Test
    public void whenUpdateSuccess_returnTrue() throws Exception {
        Long id = 1L;
        String sfid = "Success";
        String uri = "TestCreatBudget";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());
        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(203);
        responseStatus.setSuccess(true);
        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.save(budget)).willReturn(budget);
        given(utilCallApi.patchRestTemplate(uri+"/"+sfid, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);

        Boolean resultTest = budgetServiceImp.updateBudget(budget);
        assertThat(resultTest).isEqualTo(true);

    }

    @Test
    public void whenUpdateFailBecauseCallApiToSalesforceReturn500_returnFalse() throws CallApiException, Exception {
        Long id = 1L;
        String sfid = "Success";
        String uri = "TestCreatBudget";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());
        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(500);
        responseStatus.setSuccess(false);
        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.save(budget)).willReturn(budget);
        given(utilCallApi.patchRestTemplate(uri+"/"+sfid, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        Boolean resultTest = null;
        try {
            budgetServiceImp.updateBudget(budget);
        }
        catch (Exception ex) {
            if(ex instanceof CallApiException) resultTest = false;
        }
        assertThat(resultTest).isEqualTo(false);
    }


    @Test
    public void whenExpiredTokenUpdateBudget_thenRefreshToken() throws Exception {
        Long id = 1L;
        String uri = "TestCreatBudget";
        String sfid = "Success";
        String tokenRefresh = "refresh";
        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(401);
        responseStatus.setSuccess(false);

        ResponseStatus responseStatusRefreshToken = new ResponseStatus();
        responseStatusRefreshToken.setStatusCode(200);
        responseStatusRefreshToken.setSuccess(true);
        responseStatusRefreshToken.setAccessToken(tokenRefresh);

        ResponseStatus responseResultAfterReSynced = new ResponseStatus();
        responseResultAfterReSynced.setStatusCode(203);
        responseResultAfterReSynced.setSuccess(true);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.patchRestTemplate(uri+"/"+sfid, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        given(utilCallApi.refreshToken(apiConnect.getRefreshToken())).willReturn(responseStatusRefreshToken);
        given(utilCallApi.patchRestTemplate(uri+"/"+sfid, tokenRefresh, dataJson.toString())).willReturn(responseResultAfterReSynced);
        given(budgetRepository.save(budget)).willReturn(budget);

        Boolean resultTest = budgetServiceImp.updateBudget(budget);
        assertThat(resultTest).isEqualTo(true);
    }

    @Test
    public void whenUpdateFail_thenRefreshTokenButFail() throws Exception, CallApiException {
        Long id = 1L;
        String uri = "TestCreatBudget";
        String sfid = "Success";
        String tokenRefresh = "refresh";
        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());


        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(401);
        responseStatus.setSuccess(false);

        ResponseStatus responseStatusRefreshToken = new ResponseStatus();
        responseStatusRefreshToken.setStatusCode(200);
        responseStatusRefreshToken.setSuccess(true);
        responseStatusRefreshToken.setAccessToken(tokenRefresh);

        ResponseStatus responseResultAfterReSynced = new ResponseStatus();
        responseResultAfterReSynced.setStatusCode(500);
        responseResultAfterReSynced.setSuccess(false);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.patchRestTemplate(uri+"/"+sfid, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        given(utilCallApi.refreshToken(apiConnect.getRefreshToken())).willReturn(responseStatusRefreshToken);
        given(utilCallApi.patchRestTemplate(uri+"/"+sfid, tokenRefresh, dataJson.toString())).willReturn(responseResultAfterReSynced);
        Boolean resultTest = null;
        try {
            resultTest = budgetServiceImp.updateBudget(budget);
        }
        catch (Exception ex) {
            if(ex instanceof CallApiException) resultTest = false;
        }
        assertThat(resultTest).isEqualTo(false);
    }

    @Test
    public void whenDeleteSucces_returnTrue() throws Exception {
        Long id = 1L;
        String sfid = "Success";
        String uri = "TestCreatBudget";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());

        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(203);
        responseStatus.setSuccess(true);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(budgetRepository.findById(id)).willReturn(java.util.Optional.of(budget));
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.save(budget)).willReturn(budget);
        given(utilCallApi.deleteRestTemplate(uri+"/"+sfid, apiConnect.getToken())).willReturn(responseStatus);

        Boolean resultTest = budgetServiceImp.deleteBudget(id);
        assertThat(resultTest).isEqualTo(true);
    }

    @Test
    public void whenDeleteFailBecauseServerError_returnFalse() throws Exception {
        Long id = 1L;
        String sfid = "Success";
        String uri = "TestCreatBudget";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());

        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(500);
        responseStatus.setSuccess(false);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(budgetRepository.findById(id)).willReturn(java.util.Optional.of(budget));
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.save(budget)).willReturn(budget);
        given(utilCallApi.deleteRestTemplate(uri+"/"+sfid, apiConnect.getToken())).willReturn(responseStatus);

        Boolean resultTest = null;
        try {
            resultTest = budgetServiceImp.deleteBudget(id);
        }
        catch (Exception ex) {
            if(ex instanceof CallApiException) resultTest = false;
        }
        assertThat(resultTest).isEqualTo(false);
    }

    @Test
    public void whenDeleteFailTokenNotExist_returnFalse() throws Exception {
        Long id = 1L;
        String sfid = "Success";
        String uri = "TestCreatBudget";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(budgetRepository.findById(id)).willReturn(java.util.Optional.of(budget));
        given(apiConnectRepository.getExpired(false)).willReturn(null);

        Boolean resultTest = null;
        try {
            resultTest = budgetServiceImp.deleteBudget(id);
        }
        catch (Exception ex) {
            if(ex instanceof CallApiException) resultTest = false;
        }
        assertThat(resultTest).isEqualTo(false);
    }

    @Test
    public void whenExpiredTokenDeleteBudget_thenRefreshToken() throws Exception {
        Long id = 1L;
        String sfid = "Success";
        String uri = "TestCreatBudget";
        String tokenRefresh = "refresh";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());

        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(401);
        responseStatus.setSuccess(false);

        ResponseStatus responseStatusRefreshToken = new ResponseStatus();
        responseStatusRefreshToken.setStatusCode(200);
        responseStatusRefreshToken.setSuccess(true);
        responseStatusRefreshToken.setAccessToken(tokenRefresh);

        ResponseStatus responseResultAfterReSynced = new ResponseStatus();
        responseResultAfterReSynced.setStatusCode(203);
        responseResultAfterReSynced.setSuccess(true);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(budgetRepository.findById(id)).willReturn(java.util.Optional.of(budget));
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.save(budget)).willReturn(budget);
        given(utilCallApi.deleteRestTemplate(uri+"/"+sfid, apiConnect.getToken())).willReturn(responseStatus);
        given(utilCallApi.refreshToken(apiConnect.getRefreshToken())).willReturn(responseStatusRefreshToken);
        given(utilCallApi.deleteRestTemplate(uri+"/"+sfid, tokenRefresh)).willReturn(responseResultAfterReSynced);

        Boolean resultTest = budgetServiceImp.deleteBudget(id);
        assertThat(resultTest).isEqualTo(true);
    }

    @Test
    public void whenExpiredTokenDeleteBudget_thenRefreshTokenButFail() throws Exception {
        Long id = 1L;
        String sfid = "Success";
        String uri = "TestCreatBudget";
        String tokenRefresh = "refresh";
        Budget budget = new Budget("Test1", "2001");
        budget.setId(id);
        budget.setSfid(sfid);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", budget.getName());
        dataJson.put("Year__c", budget.getYear());

        ApiConnect apiConnect = new ApiConnect("Test", "Test", false);
        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(401);
        responseStatus.setSuccess(false);

        ResponseStatus responseStatusRefreshToken = new ResponseStatus();
        responseStatusRefreshToken.setStatusCode(200);
        responseStatusRefreshToken.setSuccess(true);
        responseStatusRefreshToken.setAccessToken(tokenRefresh);

        ResponseStatus responseResultAfterReSynced = new ResponseStatus();
        responseResultAfterReSynced.setStatusCode(500);
        responseResultAfterReSynced.setSuccess(false);

        FieldSetter.setField(budgetServiceImp, budgetServiceImp.getClass().getDeclaredField("uri"), uri);
        given(config.getAppUri()).willReturn(uri);
        given(budgetRepository.findById(id)).willReturn(java.util.Optional.of(budget));
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(budgetRepository.save(budget)).willReturn(budget);
        given(utilCallApi.deleteRestTemplate(uri+"/"+sfid, apiConnect.getToken())).willReturn(responseStatus);
        given(utilCallApi.refreshToken(apiConnect.getRefreshToken())).willReturn(responseStatusRefreshToken);
        given(utilCallApi.deleteRestTemplate(uri+"/"+sfid, tokenRefresh)).willReturn(responseResultAfterReSynced);
        Boolean resultTest = null;
        try {
            resultTest = budgetServiceImp.deleteBudget(id);
        }
        catch (Exception ex) {
            if(ex instanceof CallApiException) resultTest = false;
        }
        assertThat(resultTest).isEqualTo(false);
    }
}
