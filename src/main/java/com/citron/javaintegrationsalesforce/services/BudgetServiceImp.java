package com.citron.javaintegrationsalesforce.services;

import com.citron.javaintegrationsalesforce.config.ConnectSalesforceConfig;
import com.citron.javaintegrationsalesforce.controller.AuthController;
import com.citron.javaintegrationsalesforce.model.ApiConnect;
import com.citron.javaintegrationsalesforce.model.Budget;
import com.citron.javaintegrationsalesforce.model.ProposalBudget;
import com.citron.javaintegrationsalesforce.model.ResponseStatus;
import com.citron.javaintegrationsalesforce.repository.ApiConnectRepository;
import com.citron.javaintegrationsalesforce.repository.BudgetRepository;
import com.citron.javaintegrationsalesforce.repository.ProposalBudgetRepository;
import com.citron.javaintegrationsalesforce.util.UtilCallApi;
import com.citron.javaintegrationsalesforce.util.exception.CallApiException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class BudgetServiceImp implements BudgetService {
    private final static java.util.logging.Logger logger = Logger.getLogger(AuthController.class.getName());
    private String uri;
    @Autowired
    private ApiConnectRepository apiConnectRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ConnectSalesforceConfig config;

    @Autowired
    private ProposalBudgetRepository proposalBudgetRepository;

    @Autowired
    private UtilCallApi utilCallApi;

    public BudgetServiceImp(ApiConnectRepository apiConnectRepository, BudgetRepository budgetRepository, ConnectSalesforceConfig config, ProposalBudgetRepository _proposalBudgetRepository, UtilCallApi _utilCallApi) {
        this.apiConnectRepository = apiConnectRepository;
        this.budgetRepository = budgetRepository;
        this.config = config;
        this.proposalBudgetRepository = _proposalBudgetRepository;
        this.utilCallApi = _utilCallApi;
        this.uri = config.getAppUri()+"/Budget__c";
    }

    public List<Budget> getAllBudget() {
        return budgetRepository.findAll();
    }

    public ApiConnectRepository getApiConnect() {
        return apiConnectRepository;
    }

    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id).get();
    }

    public List<ProposalBudget> getJunction(String sfid) {
        return proposalBudgetRepository.getJunctionFromBudget(sfid);
    }

    @Override
    public Long createBudget(Budget budget) throws Exception {
        try {
            String sfid = "";
            ApiConnect infoConnect = apiConnectRepository.getExpired(false);
            if(infoConnect == null) {
                throw new CallApiException("Your application is not connected to salesforce or access token expired");
            }
            JSONObject dataJson = new JSONObject();
            dataJson.put("Name", budget.getName());
            dataJson.put("Year__c", budget.getYear());

            ResponseStatus response = utilCallApi.postRestTemplate(uri, infoConnect.getToken(), dataJson.toString());
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(infoConnect.getRefreshToken());
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();

                        ResponseStatus response1 = utilCallApi.postRestTemplate(uri, access_token, dataJson.toString());
                        if (response1.getSuccess() == true) {
                            sfid = response1.getId();
                        }
                    }
                }
            } else if (response.getSuccess() == true) {
                sfid = response.getId();
            }

            if(!sfid.isEmpty()) {
                budget.setSfid(sfid);
                budget.setTotalAmount(0.0);
                Budget createdBudget = budgetRepository.saveAndFlush(budget);
                return createdBudget.getId();
            }

        }
        catch(Exception ex) {
            System.out.println(ex.getMessage());
            logger.warning(ex.getMessage());
            logger.info(this.getClass().getName()+": "+ex.getCause()+", "+ex.getStackTrace()[0].getLineNumber());
            if(ex instanceof CallApiException) throw new CallApiException(ex);
        }
        return 0L;
    }

    @Override
    public Boolean updateBudget(Budget budget) throws Exception {
        try {
            String currentSfid = budget.getSfid();
            Boolean flagUpdate = false;
            ApiConnect infoConnect = apiConnectRepository.getExpired(false);
            if(infoConnect == null) {
                throw new CallApiException("Your application is not connected to salesforce or access token expired");
            }
            JSONObject dataJson = new JSONObject();
            dataJson.put("Name", budget.getName());
            dataJson.put("Year__c", budget.getYear());

            ResponseStatus response = utilCallApi.patchRestTemplate(uri+"/"+currentSfid, infoConnect.getToken(), dataJson.toString());
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(infoConnect.getRefreshToken());
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();
                        ResponseStatus response1 = utilCallApi.patchRestTemplate(uri+"/"+currentSfid, access_token, dataJson.toString());
                        if (response1.getSuccess() == true) {
                            flagUpdate = true;
                        }
                    }
                }
            } else if (response.getSuccess() == true) {
                flagUpdate = true;
            }

            if(flagUpdate == false) {
                throw new CallApiException("failed update");
            }

            budgetRepository.save(budget);
            return true;

        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
            logger.warning(ex.getMessage());
            logger.info(this.getClass().getName()+": "+ex.getCause()+", "+ex.getStackTrace()[0].getLineNumber());
            if(ex instanceof CallApiException) throw new CallApiException(ex);
        }
        return false;
    }

    @Override
    public Boolean deleteBudget(Long id) throws Exception {
        try{
            Budget budget = budgetRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Proposal Id:" + id));

            ApiConnect infoConnect = apiConnectRepository.getExpired(false);
            String currentSfid = budget.getSfid();
            Boolean flagDelete = false;

            ResponseStatus response = utilCallApi.deleteRestTemplate(uri+"/"+currentSfid, infoConnect.getToken() );
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(infoConnect.getRefreshToken());
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();
                        ResponseStatus response1 = utilCallApi.deleteRestTemplate(uri+"/"+currentSfid, access_token);
                        if (response1.getSuccess() == true) {
                            flagDelete = true;
                        }
                    }
                }
            } else if (response.getSuccess() == true) {
                flagDelete = true;
            }

            if(flagDelete == false) {
                throw new CallApiException("delete failed");
            }

            budgetRepository.delete(budget);
            return true;
        }
        catch (Exception ex) {
            System.out.print(ex.getMessage());
            logger.warning(ex.getMessage());
            logger.info(this.getClass().getName()+": "+ex.getCause()+", "+ex.getStackTrace()[0].getLineNumber());
            if(ex instanceof CallApiException) throw new CallApiException(ex);
        }
        return false;
    }
}
