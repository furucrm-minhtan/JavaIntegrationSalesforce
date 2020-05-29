package com.citron.javaintegrationsalesforce.controller;

import com.citron.javaintegrationsalesforce.config.ConnectSalesforceConfig;
import com.citron.javaintegrationsalesforce.model.*;
import com.citron.javaintegrationsalesforce.model.ResponseStatus;
import com.citron.javaintegrationsalesforce.repository.ApiConnectRepository;
import com.citron.javaintegrationsalesforce.repository.BudgetRepository;
import com.citron.javaintegrationsalesforce.repository.ProposalBudgetRepository;
import com.citron.javaintegrationsalesforce.repository.ProposalRepository;
import com.citron.javaintegrationsalesforce.util.UtilCallApi;
import com.citron.javaintegrationsalesforce.util.UtilClass;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class ProposalController {

    static final String URL_CREATE_PROPOSAL = "https://eap-prototype-dev-ed.my.salesforce.com/services/data/v48.0/sobjects/Proposal__c";

    @Autowired
    ProposalRepository proposalRepository;
    @Autowired
    ProposalBudgetRepository proposalBudgetRepository;
    @Autowired
    ApiConnectRepository apiConnectRepository;
    @Autowired
    UtilCallApi utilCallApi;

    public ProposalController(ApiConnectRepository apiConnectRepository, ProposalRepository proposalRepository, ProposalBudgetRepository proposalBudgetRepository, UtilCallApi utilCallApi) {
        this.apiConnectRepository = apiConnectRepository;
        this.proposalRepository = proposalRepository;
        this.proposalBudgetRepository = proposalBudgetRepository;
        this.utilCallApi = utilCallApi;
    }

    @GetMapping("/proposal")
    public String index(Model model) {
        try {
            model.addAttribute("listProposal", proposalRepository.findAll());
            return "proposal/list";
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "Auth/404";
        }
    }

    @GetMapping("/proposal/{id}")
    public String show(@PathVariable("id") long id, Model model) {
        try {
            Proposal proposal = proposalRepository.findById(id).get();
            List<ProposalBudget> listProposalBudget = proposalBudgetRepository.getJunctionFromProposal(proposal.getSfid());
            model.addAttribute("proposal", proposal);
            model.addAttribute("listProposalBudget", listProposalBudget);
            return "proposal/show";
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "Auth/404";
        }

    }

    @RequestMapping(path = "proposal/create", method = RequestMethod.GET)
    public String createProposal(Model model) {
        model.addAttribute("apiConnect", apiConnectRepository.getExpired(false));
        model.addAttribute("proposal", new ProposalWrapper());
        return "proposal/create";
    }

    @RequestMapping(path = "proposal/create", method = RequestMethod.POST)
    public String createProposal(@Valid @ModelAttribute("proposal") ProposalWrapper proposal, BindingResult bindingresult, Model model) throws JSONException {

        ApiConnect apiConnect = apiConnectRepository.getExpired(false);

        if(bindingresult.hasErrors()) {
            model.addAttribute("apiConnect", apiConnect);
            return "proposal/create";
        }

        String sfid = "";

        if(apiConnect != null) {

            String token = apiConnect.getToken();
            String refresh_token = apiConnect.getRefreshToken();

            JSONObject dataJson = new JSONObject();
            dataJson.put("Name", proposal.getName());
            dataJson.put("Year__c", proposal.getYear());
            dataJson.put("Approved_At__c", utilCallApi.convertDateTimeApi(proposal.getApprovedAt()));
            dataJson.put("Proposed_At__c", utilCallApi.convertDateTimeApi(proposal.getProposedAt()));
            dataJson.put("Details__c", proposal.getDetail());

//            ResponseStatus response = UtilClass.postRestTemplate(URL_CREATE_PROPOSAL, token, dataJson.toString());
            ResponseStatus response = utilCallApi.postRestTemplate(URL_CREATE_PROPOSAL, token, dataJson.toString());
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(refresh_token);
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();

                        ResponseStatus response1 = utilCallApi.postRestTemplate(URL_CREATE_PROPOSAL, access_token, dataJson.toString());
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
            return "redirect:/proposal/create";
        }

        try{
            Proposal newProposal = new Proposal();
            newProposal.setName(proposal.getName());
            newProposal.setYear(proposal.getYear());
            newProposal.setDetail((proposal.getDetail().isEmpty()) ? null : proposal.getDetail());
            newProposal.setTotalAmount(0.0);
            newProposal.setApprovedAt(utilCallApi.convertDateTimeInsert(proposal.getApprovedAt()));
            newProposal.setProposedAt(utilCallApi.convertDateTimeInsert(proposal.getProposedAt()));
            newProposal.setSfid(sfid);

            newProposal = proposalRepository.saveAndFlush(newProposal);

            return "redirect:/proposal/" + newProposal.getId();

        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/proposal";
        }
    }

    @RequestMapping(path = "proposal/edit/{id}", method = RequestMethod.GET)
    public String editProposal(@PathVariable("id") long id, Model model) {
        try{
            Proposal curProposal = proposalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Proposal Id:" + id));
            ProposalWrapper proposal = new ProposalWrapper();
            proposal.setId(curProposal.getId());
            proposal.setName(curProposal.getName());
            proposal.setYear(curProposal.getYear());
            proposal.setApprovedAt(curProposal.getFormatApprovedAt());
            proposal.setProposedAt(curProposal.getFormatProposedAt());
            proposal.setDetail(curProposal.getDetail());
            proposal.setSfid(curProposal.getSfid());
            proposal.setTotalAmount(curProposal.getTotalAmount());
            model.addAttribute("proposal", proposal);
            model.addAttribute("apiConnect", apiConnectRepository.getExpired(false));
            return "proposal/edit";

        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "Auth/404";
        }
    }

    @RequestMapping(path = "proposal/edit/{id}", method = RequestMethod.POST)
    public String editProposal(@Valid @PathVariable("id") long id, @ModelAttribute("proposal") ProposalWrapper proposal, Model model, BindingResult bindingresult) throws JSONException {

        ApiConnect apiConnect = apiConnectRepository.getExpired(false);

        if(bindingresult.hasErrors()) {
            model.addAttribute("apiConnect", apiConnect);
            return "proposal/edit/" + id;
        }

        Boolean flagUpdate = false;

        if(apiConnect != null) {
            String currentSfid = proposal.getSfid();

            String token = apiConnect.getToken();
            String refresh_token = apiConnect.getRefreshToken();

            JSONObject dataJson = new JSONObject();
            dataJson.put("Name", proposal.getName());
            dataJson.put("Year__c", proposal.getYear());
            dataJson.put("Approved_At__c", utilCallApi.convertDateTimeApi(proposal.getApprovedAt()));
            dataJson.put("Proposed_At__c", utilCallApi.convertDateTimeApi(proposal.getProposedAt()));
            dataJson.put("Details__c", proposal.getDetail());

            ResponseStatus response = utilCallApi.patchRestTemplate(URL_CREATE_PROPOSAL+"/"+currentSfid, token, dataJson.toString());
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(refresh_token);
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();

                        ResponseStatus response1 = utilCallApi.patchRestTemplate(URL_CREATE_PROPOSAL+"/"+currentSfid, access_token, dataJson.toString());
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
            return "redirect:/proposal/edit/" + id;
        }

        try{
            proposal.setDetail((proposal.getDetail().isEmpty()) ? null : proposal.getDetail());
            Proposal newProposal = new Proposal(proposal);
            proposalRepository.save(newProposal);

            return "redirect:/proposal/"+newProposal.getId();
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/proposal/edit/"+id;
        }
    }

    @GetMapping("/proposal/destroy/{id}")
    public String deleteProposal(@PathVariable("id") long id, Model model) throws JSONException {

        ApiConnect apiConnect = apiConnectRepository.getExpired(false);
        Proposal proposal = proposalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Proposal Id:" + id));
        String currentSfid = proposal.getSfid();
        Boolean flagDelete = false;

        if(apiConnect != null) {
            String token = apiConnect.getToken();
            String refresh_token = apiConnect.getRefreshToken();

            ResponseStatus response = utilCallApi.deleteRestTemplate(URL_CREATE_PROPOSAL+"/"+currentSfid, token);
            if(response.getSuccess() == false) {
                if(response.getStatusCode() == 401) {
                    ResponseStatus responseRefresh = utilCallApi.refreshToken(refresh_token);
                    if(responseRefresh.getSuccess() == true) {
                        String access_token = responseRefresh.getAccessToken();

                        ResponseStatus response1 = utilCallApi.deleteRestTemplate(URL_CREATE_PROPOSAL+"/"+currentSfid, access_token);
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
            return "redirect:/proposal";
        }

        try{
            proposalRepository.delete(proposal);
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return "redirect:/proposal";
    }
}
