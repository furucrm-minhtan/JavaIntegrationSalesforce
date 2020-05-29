package com.citron.javaintegrationsalesforce;

import com.citron.javaintegrationsalesforce.controller.ProposalController;
import com.citron.javaintegrationsalesforce.model.*;
import com.citron.javaintegrationsalesforce.repository.ApiConnectRepository;
import com.citron.javaintegrationsalesforce.repository.ProposalBudgetRepository;
import com.citron.javaintegrationsalesforce.repository.ProposalRepository;
import com.citron.javaintegrationsalesforce.util.UtilCallApi;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@WebMvcTest(ProposalController.class)
public class ProposalControllerTest {

    static final String URL_CREATE_PROPOSAL = "https://eap-prototype-dev-ed.my.salesforce.com/services/data/v48.0/sobjects/Proposal__c";
    private MockMvc mockMvc;

    @InjectMocks
    private ProposalController proposalController;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private ProposalBudgetRepository proposalBudgetRepository;
    @Mock
    private ApiConnectRepository apiConnectRepository;
    @Mock
    private UtilCallApi utilCallApi;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(proposalController).build();
    }

    @Test
    public void whenGetListProposal_thenReturnSuccess() throws Exception {

        ArrayList<Proposal> listProposal = new ArrayList<>();
        Proposal p1 = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        Proposal p2 = new Proposal("Test2", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test2");
        listProposal.add(p1);
        listProposal.add(p2);

        given(proposalRepository.findAll()).willReturn(listProposal);

        mockMvc.perform(get("/proposal").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name("proposal/list"))
                .andExpect(model().attribute("listProposal", hasSize(2)));
    }

    @Test
    public void whenGetShowProposal_thenReturnSuccess() throws Exception {
        Proposal p1 = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");

        ArrayList<ProposalBudget> listPB = new ArrayList<>();
        ProposalBudget pb1 = new ProposalBudget("Test1", "Test1", Double.parseDouble("1"), "Test1");
        ProposalBudget pb2 = new ProposalBudget("Test2", "Test2", Double.parseDouble("1"), "Test2");
        listPB.add(pb1);
        listPB.add(pb2);

        given(proposalRepository.findById(1L)).willReturn(java.util.Optional.of(p1));
        given(proposalBudgetRepository.getJunctionFromProposal(p1.getSfid())).willReturn(listPB);

        mockMvc.perform(get("/proposal/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("proposal/show"))
                .andExpect(model().attribute("proposal", p1))
                .andExpect(model().attribute("listProposalBudget", hasSize(2)));

    }

    @Test
    public void whenGetShowProposal_thenReturnException() throws Exception {

        given(proposalRepository.findById(1L)).willReturn(null);

        mockMvc.perform(get("/proposal/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("Auth/404"));
    }

    @Test
    public void whenGetCreateProposal_thenReturnSuccess() throws Exception {

        ApiConnect connect = new ApiConnect("test", "test", false);
        given(apiConnectRepository.getExpired(false)).willReturn(connect);

        mockMvc.perform(get("/proposal/create"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("apiConnect", connect))
                .andExpect(view().name("proposal/create"));
    }

    @Test
    public void whenPostCreateProposal_thenReturnSuccess() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        ProposalWrapper proposal = new ProposalWrapper("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", 0.0, "" , "test1");

        Proposal newProposal = new Proposal(proposal);
        newProposal.setId(id);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);
        responseStatus.setId(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", proposal.getName());
        dataJson.put("Year__c", proposal.getYear());
        dataJson.put("Approved_At__c", utilCallApi.convertDateTimeApi(proposal.getApprovedAt()));
        dataJson.put("Proposed_At__c", utilCallApi.convertDateTimeApi(proposal.getProposedAt()));
        dataJson.put("Details__c", proposal.getDetail());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.postRestTemplate(URL_CREATE_PROPOSAL, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        when(proposalRepository.saveAndFlush(any(Proposal.class))).thenReturn(newProposal);

        mockMvc.perform(
                post("/proposal/create").flashAttr("proposal", proposal).with(csrf())
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposal/1"));
    }

    @Test
    public void whenPostCreateProposal_thenReturnFalseNullSfid() throws Exception {

        ProposalWrapper proposal = new ProposalWrapper("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", 0.0, "" , "test1");

        given(apiConnectRepository.getExpired(false)).willReturn(null);

        mockMvc.perform(
                post("/proposal/create").flashAttr("proposal", proposal).with(csrf())
        ).andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposal/create"));
    }

    @Test
    public void whenPostCreateProposal_thenReturnFalseTryCatch() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        ProposalWrapper proposal = new ProposalWrapper("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", 0.0, "" , "test1");

        Proposal newProposal = new Proposal(proposal);
        newProposal.setId(id);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);
        responseStatus.setId(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", proposal.getName());
        dataJson.put("Year__c", proposal.getYear());
        dataJson.put("Approved_At__c", utilCallApi.convertDateTimeApi(proposal.getApprovedAt()));
        dataJson.put("Proposed_At__c", utilCallApi.convertDateTimeApi(proposal.getProposedAt()));
        dataJson.put("Details__c", proposal.getDetail());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.postRestTemplate(URL_CREATE_PROPOSAL, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);

        mockMvc.perform(
                post("/proposal/create").flashAttr("proposal", proposal).with(csrf())
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposal"));
    }

    @Test
    public void whenGetEditProposal_thenReturnSuccess() throws Exception {

        Long id = 1L;
        ApiConnect connect = new ApiConnect("test", "test", false);
        Proposal p1 = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        p1.setId(id);

        given(proposalRepository.findById(1L)).willReturn(java.util.Optional.of(p1));
        given(apiConnectRepository.getExpired(false)).willReturn(connect);

        mockMvc.perform(get("/proposal/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("proposal/edit"))
                .andExpect(model().attribute("apiConnect", connect));
    }

    @Test
    public void whenGetEditProposal_thenReturnFalseTryCatch() throws Exception {

        when(proposalRepository.findById(1L)).thenThrow(new ArrayIndexOutOfBoundsException());

        mockMvc.perform(get("/proposal/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("Auth/404"));
    }

    @Test
    public void whenPostUpdateProposal_thenReturnSuccess() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        ProposalWrapper proposal = new ProposalWrapper("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", 0.0, "" , "test1");
        proposal.setId(id);

        Proposal newProposal = new Proposal(proposal);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);
        responseStatus.setId(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", proposal.getName());
        dataJson.put("Year__c", proposal.getYear());
        dataJson.put("Approved_At__c", utilCallApi.convertDateTimeApi(proposal.getApprovedAt()));
        dataJson.put("Proposed_At__c", utilCallApi.convertDateTimeApi(proposal.getProposedAt()));
        dataJson.put("Details__c", proposal.getDetail());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.patchRestTemplate(URL_CREATE_PROPOSAL+"/"+proposal.getSfid(), apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        when(proposalRepository.save(any(Proposal.class))).thenReturn(newProposal);

        mockMvc.perform(
                post("/proposal/edit/{id}", 1).flashAttr("proposal", proposal)
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposal/1"));
    }

    @Test
    public void whenPostUpdateProposal_thenReturnFalseFlagUpdate() throws Exception {

        Long id = 1L;

        ProposalWrapper proposal = new ProposalWrapper("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", 0.0, "" , "test1");
        proposal.setId(id);

        given(apiConnectRepository.getExpired(false)).willReturn(null);

        mockMvc.perform(
                post("/proposal/edit/{id}", 1).flashAttr("proposal", proposal)
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposal/edit/1"));
    }

    @Test
    public void whenPostUpdateProposal_thenReturnFalseTryCatch() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        ProposalWrapper proposal = new ProposalWrapper("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", 0.0, "" , "test1");
        proposal.setId(id);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);
        responseStatus.setId(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Name", proposal.getName());
        dataJson.put("Year__c", proposal.getYear());
        dataJson.put("Approved_At__c", utilCallApi.convertDateTimeApi(proposal.getApprovedAt()));
        dataJson.put("Proposed_At__c", utilCallApi.convertDateTimeApi(proposal.getProposedAt()));
        dataJson.put("Details__c", proposal.getDetail());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.patchRestTemplate(URL_CREATE_PROPOSAL+"/"+proposal.getSfid(), apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        when(proposalRepository.save(any())).thenThrow(new ArrayIndexOutOfBoundsException());

        mockMvc.perform(
                post("/proposal/edit/{id}", 1).flashAttr("proposal", proposal)
        ).andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposal/edit/1"));
    }

    @Test
    public void whenGetDeleteProposal_thenReturnSuccess() throws Exception {

        Long id = 1L;
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        Proposal p1 = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        p1.setId(id);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);

        given(proposalRepository.findById(1L)).willReturn(java.util.Optional.of(p1));
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.deleteRestTemplate(URL_CREATE_PROPOSAL+"/"+p1.getSfid(), apiConnect.getToken())).willReturn(responseStatus);

        mockMvc.perform(get("/proposal/destroy/{id}", 1))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposal"));
    }

    @Test
    public void whenGetDeleteProposal_thenReturnFalseFlagDelete() throws Exception {

        Long id = 1L;
        Proposal p1 = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        p1.setId(id);

        given(proposalRepository.findById(1L)).willReturn(java.util.Optional.of(p1));
        given(apiConnectRepository.getExpired(false)).willReturn(null);

        mockMvc.perform(get("/proposal/destroy/{id}", 1))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposal"));
    }
}
