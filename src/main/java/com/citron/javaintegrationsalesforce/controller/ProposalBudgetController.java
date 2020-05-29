package com.citron.javaintegrationsalesforce.controller;

import com.citron.javaintegrationsalesforce.model.*;
import com.citron.javaintegrationsalesforce.model.ResponseStatus;
import com.citron.javaintegrationsalesforce.repository.*;
import com.citron.javaintegrationsalesforce.util.UtilCallApi;
import com.citron.javaintegrationsalesforce.util.UtilClass;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class ProposalBudgetController {

    static final String URL_CREATE_JUNCTION = "https://eap-prototype-dev-ed.my.salesforce.com/services/data/v48.0/sobjects/Proposal_Budget__c";

    @Autowired
    ProposalRepository proposalRepository;
    @Autowired
    BudgetRepository budgetRepository;
    @Autowired
    ProposalBudgetRepository proposalBudgetRepository;
    @Autowired
    JunctionRepository junctionRepository;
    @Autowired
    ApiConnectRepository apiConnectRepository;
    @Autowired
    UtilCallApi utilCallApi;

    public ProposalBudgetController(ApiConnectRepository apiConnectRepository, ProposalRepository proposalRepository,
                                    ProposalBudgetRepository proposalBudgetRepository, UtilCallApi utilCallApi,
                                    BudgetRepository budgetRepository, JunctionRepository junctionRepository) {
        this.apiConnectRepository = apiConnectRepository;
        this.proposalRepository = proposalRepository;
        this.proposalBudgetRepository = proposalBudgetRepository;
        this.utilCallApi = utilCallApi;
        this.budgetRepository = budgetRepository;
        this.junctionRepository = junctionRepository;
    }

    @GetMapping("/proposalbudget/create/{url}")
    public String show(@PathVariable("url") String url, Model model) {
        try {
            JunctionWrapper junction = new JunctionWrapper();

            String[] dataCheckType = url.split("-");
            String typeRedirect = "";
            if(dataCheckType[0].equals("proposal")) {
                typeRedirect = "proposal";
                Proposal proposal = proposalRepository.findById(Long.parseLong(dataCheckType[1])).get();
                junction.setProposalId(proposal.getSfid());
                junction.setTypeRedirect(typeRedirect);
                junction.setIdRedirect(dataCheckType[1]);
            } else if(dataCheckType[0].equals("budget")) {
                typeRedirect = "budget";
                Budget budget = budgetRepository.findById(Long.parseLong(dataCheckType[1])).get();
                junction.setBudgetId(budget.getSfid());
                junction.setTypeRedirect(typeRedirect);
                junction.setIdRedirect(dataCheckType[1]);
            } else {
                return "Auth/404";
            }

            List<Proposal> listProposal = proposalRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
            List<Budget> listBudget = budgetRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

            model.addAttribute("apiConnect", apiConnectRepository.getExpired(false));
            model.addAttribute("junction", junction);
            model.addAttribute("listProposal", listProposal);
            model.addAttribute("listBudget", listBudget);

            return "proposal_budget/create";
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/proposal";
        }

    }

    @PostMapping("/proposalbudget/store")
    public String createJunction(@Valid @ModelAttribute("junction") JunctionWrapper junction, BindingResult bindingresult, Model model) throws JSONException {

        ApiConnect apiConnect = apiConnectRepository.getExpired(false);

        if(bindingresult.hasErrors()) {
            model.addAttribute("apiConnect", apiConnect);
            return "redirect:/proposalbudget/create/" + junction.getTypeRedirect() + "-" + junction.getIdRedirect();
        }

        String sfid = "";

        if(apiConnect != null) {

            String token = apiConnect.getToken();
            String refresh_token = apiConnect.getRefreshToken();

            JSONObject dataJson = new JSONObject();
            dataJson.put("Budget__c", junction.getBudgetId());
            dataJson.put("Proposal__c", junction.getProposalId());
            dataJson.put("Amount__c", junction.getAmount());

            ResponseStatus response = utilCallApi.postRestTemplate(URL_CREATE_JUNCTION, token, dataJson.toString());
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(refresh_token);
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();

                        ResponseStatus response1 = utilCallApi.postRestTemplate(URL_CREATE_JUNCTION, access_token, dataJson.toString());
                        if (response1.getSuccess() == true) {
                            sfid = response1.getId();
                        }
                    }
                }
            } else if (response.getSuccess() == true) {
                sfid = response.getId();
            }
        }

        if(sfid.isEmpty()) {
            return "redirect:/proposalbudget/create/" + junction.getTypeRedirect() + "-" + junction.getIdRedirect();
        }

        try{
            Junction newJunction = new Junction();
            newJunction.setBudgetId(junction.getBudgetId());
            newJunction.setProposalId(junction.getProposalId());
            newJunction.setAmount(Double.parseDouble(junction.getAmount()));
            newJunction.setSfid(sfid);

            newJunction = junctionRepository.saveAndFlush(newJunction);

            return "redirect:/" + junction.getTypeRedirect() + "/" + junction.getIdRedirect();

        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/proposalbudget/create/" + junction.getTypeRedirect() + "-" + junction.getIdRedirect();
        }
    }

    @GetMapping("/proposalbudget/edit/{url}")
    public String editJunction(@PathVariable("url") String url, Model model) {
        try {
            JunctionWrapper junction = new JunctionWrapper();
            String[] dataCheckType = url.split("-");
            junction.setTypeRedirect(dataCheckType[0]);

            Junction curJunction = junctionRepository.findById(Long.parseLong(dataCheckType[1])).orElseThrow(() -> new IllegalArgumentException("Invalid Junction"));

            junction.setId(curJunction.getId());
            junction.setProposalId(curJunction.getProposalId());
            junction.setBudgetId(curJunction.getBudgetId());
            junction.setAmount(curJunction.getAmount().toString());
            junction.setSfid(curJunction.getSfid());

            List<Proposal> listProposal = proposalRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
            List<Budget> listBudget = budgetRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

            model.addAttribute("junction", junction);
            model.addAttribute("apiConnect", apiConnectRepository.getExpired(false));
            model.addAttribute("listProposal", listProposal);
            model.addAttribute("listBudget", listBudget);
            return "proposal_budget/edit";
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/proposal";
        }
    }

    @PostMapping("/proposalbudget/update/{id}")
    public String editJunction(@Valid @PathVariable("id") long id, @ModelAttribute("junction") JunctionWrapper junction, Model model, BindingResult bindingresult) throws JSONException {

        ApiConnect apiConnect = apiConnectRepository.getExpired(false);

        if(bindingresult.hasErrors()) {
            model.addAttribute("apiConnect", apiConnect);
            return "redirect:/proposal";// cần sửa lại
        }

        Boolean flagUpdate = false;

        if(apiConnect != null) {

            String currentSfid = junction.getSfid();
            String token = apiConnect.getToken();
            String refresh_token = apiConnect.getRefreshToken();

            JSONObject dataJson = new JSONObject();
            dataJson.put("Budget__c", junction.getBudgetId());
            dataJson.put("Proposal__c", junction.getProposalId());
            dataJson.put("Amount__c", junction.getAmount());

            ResponseStatus response = utilCallApi.patchRestTemplate(URL_CREATE_JUNCTION+"/"+currentSfid, token, dataJson.toString());
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(refresh_token);
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();

                        ResponseStatus response1 = utilCallApi.patchRestTemplate(URL_CREATE_JUNCTION+"/"+currentSfid, access_token, dataJson.toString());
                        if (response1.getSuccess() == true) {
                            flagUpdate = true;
                        }
                    }
                }
            } else if (response.getSuccess() == true) {
                flagUpdate = true;
            }
        }

        if(flagUpdate == false) {
            return "redirect:/proposalbudget/edit/" + junction.getTypeRedirect() + "-" + junction.getIdRedirect();
        }

        try{

            Junction newJunction = new Junction(junction);
            junctionRepository.save(newJunction);

            if(junction.getTypeRedirect().equals("proposal")) {
                Proposal proposal = proposalRepository.findBySfid(junction.getProposalId());
                junction.setIdRedirect(proposal.getId().toString());
            } else {
                Budget budget = budgetRepository.findBySfid(junction.getBudgetId());
                junction.setIdRedirect(budget.getId().toString());
            }

            return "redirect:/" + junction.getTypeRedirect() + "/" + junction.getIdRedirect();
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/proposalbudget/edit/" + junction.getTypeRedirect() + "-" + junction.getIdRedirect();
        }
    }

    @DeleteMapping("/proposalbudget/destroy/{id}")
    @ResponseBody
    public String deleteJunction(@PathVariable("id") Long id) throws JSONException{

        ApiConnect apiConnect = apiConnectRepository.getExpired(false);
        Junction junction = junctionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Proposal Id:" + id));
        String currentSfid = junction.getSfid();
        Boolean flagDelete = false;

        if(apiConnect != null) {

            String token = apiConnect.getToken();
            String refresh_token = apiConnect.getRefreshToken();

            ResponseStatus response = utilCallApi.deleteRestTemplate(URL_CREATE_JUNCTION+"/"+currentSfid, token);
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(refresh_token);
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();

                        ResponseStatus response1 = utilCallApi.deleteRestTemplate(URL_CREATE_JUNCTION+"/"+currentSfid, access_token);
                        if (response1.getSuccess() == true) {
                            flagDelete = true;
                        }
                    }
                }
            } else if (response.getSuccess() == true) {
                flagDelete = true;
            }
        }

        if(flagDelete == false) {
            return "FALSE";
        }

        try{
            junctionRepository.delete(junction);
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return "SUCCESS";
    }

}
