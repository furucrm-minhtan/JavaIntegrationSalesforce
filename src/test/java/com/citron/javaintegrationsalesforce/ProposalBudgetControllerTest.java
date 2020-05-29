package com.citron.javaintegrationsalesforce;

import com.citron.javaintegrationsalesforce.controller.ProposalBudgetController;
import com.citron.javaintegrationsalesforce.controller.ProposalController;
import com.citron.javaintegrationsalesforce.model.*;
import com.citron.javaintegrationsalesforce.repository.*;
import com.citron.javaintegrationsalesforce.util.UtilCallApi;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@WebMvcTest(ProposalBudgetController.class)
public class ProposalBudgetControllerTest {

    static final String URL_CREATE_JUNCTION = "https://eap-prototype-dev-ed.my.salesforce.com/services/data/v48.0/sobjects/Proposal_Budget__c";
    private MockMvc mockMvc;

    @InjectMocks
    private ProposalBudgetController proposalBudgetController;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private ProposalBudgetRepository proposalBudgetRepository;
    @Mock
    private ApiConnectRepository apiConnectRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private JunctionRepository junctionRepository;
    @Mock
    private UtilCallApi utilCallApi;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(proposalBudgetController).build();
    }

    @Test
    public void whenGetCreateJunction_thenReturnSuccess() throws Exception {
        Proposal proposal = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        ApiConnect connect = new ApiConnect("test", "test", false);

        ArrayList<Proposal> listProposal = new ArrayList<>();
        Proposal p1 = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        Proposal p2 = new Proposal("Test2", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test2");
        listProposal.add(p1);
        listProposal.add(p2);

        ArrayList<Budget> listBudget = new ArrayList<>();
        Budget b1 = new Budget("Test1", "2020", 0.0);
        Budget b2 = new Budget("Test2", "2020", 0.0);
        listBudget.add(b1);
        listBudget.add(b2);

        given(proposalRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).willReturn(listProposal);
        given(budgetRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).willReturn(listBudget);
        given(proposalRepository.findById(1L)).willReturn(java.util.Optional.of(proposal));
        given(apiConnectRepository.getExpired(false)).willReturn(connect);

        mockMvc.perform(get("/proposalbudget/create/{url}", "proposal-1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("apiConnect", connect))
                .andExpect(model().attribute("listProposal", hasSize(2)))
                .andExpect(model().attribute("listBudget", hasSize(2)))
                .andExpect(view().name("proposal_budget/create"));
    }

    @Test
    public void whenGetCreateJunction_thenReturnFalseUrl() throws Exception {
        mockMvc.perform(get("/proposalbudget/create/{url}", "test-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("Auth/404"));
    }

    @Test
    public void whenGetCreateJunction_thenReturnFalseTryCatch() throws Exception {

        when(proposalRepository.findById(1L)).thenThrow(new ArrayIndexOutOfBoundsException());

        mockMvc.perform(get("/proposalbudget/create/{url}", "proposal-1"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposal"));
    }

    @Test
    public void whenPostCreateJunction_thenReturnSuccess() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        JunctionWrapper junction = new JunctionWrapper();
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setAmount("1");
        junction.setTypeRedirect("proposal");
        junction.setIdRedirect("1");

        Junction newJunction = new Junction(junction);
        newJunction.setId(id);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);
        responseStatus.setId(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Budget__c", junction.getBudgetId());
        dataJson.put("Proposal__c", junction.getProposalId());
        dataJson.put("Amount__c", junction.getAmount());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.postRestTemplate(URL_CREATE_JUNCTION, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        when(junctionRepository.saveAndFlush(any(Junction.class))).thenReturn(newJunction);

        mockMvc.perform(
                post("/proposalbudget/store").flashAttr("junction", junction).with(csrf())
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposal/1"));
    }

    @Test
    public void whenPostCreateJunction_thenReturnFalseNullSfid() throws Exception {

        JunctionWrapper junction = new JunctionWrapper();
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setAmount("1");
        junction.setTypeRedirect("proposal");
        junction.setIdRedirect("1");

        given(apiConnectRepository.getExpired(false)).willReturn(null);

        mockMvc.perform(
                post("/proposalbudget/store").flashAttr("junction", junction).with(csrf())
        ).andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposalbudget/create/proposal-1"));
    }

    @Test
    public void whenPostCreateJunction_thenReturnFalseTryCatch() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        JunctionWrapper junction = new JunctionWrapper();
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setAmount("1");
        junction.setTypeRedirect("proposal");
        junction.setIdRedirect("1");

        Junction newJunction = new Junction(junction);
        newJunction.setId(id);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);
        responseStatus.setId(sfid);
        JSONObject dataJson = new JSONObject();
        dataJson.put("Budget__c", junction.getBudgetId());
        dataJson.put("Proposal__c", junction.getProposalId());
        dataJson.put("Amount__c", junction.getAmount());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.postRestTemplate(URL_CREATE_JUNCTION, apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        when(junctionRepository.saveAndFlush(any(Junction.class))).thenThrow(new ArrayIndexOutOfBoundsException());

        mockMvc.perform(
                post("/proposalbudget/store").flashAttr("junction", junction).with(csrf())
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposalbudget/create/proposal-1"));
    }

    @Test
    public void whenGetEditJunction_thenReturnSuccess() throws Exception {
        Long id = 1L;
        Junction junction = new Junction();
        junction.setId(id);
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setAmount(0.0);
        junction.setSfid("Test1");

        ApiConnect connect = new ApiConnect("test", "test", false);

        ArrayList<Proposal> listProposal = new ArrayList<>();
        Proposal p1 = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        Proposal p2 = new Proposal("Test2", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test2");
        listProposal.add(p1);
        listProposal.add(p2);

        ArrayList<Budget> listBudget = new ArrayList<>();
        Budget b1 = new Budget("Test1", "2020", 0.0);
        Budget b2 = new Budget("Test2", "2020", 0.0);
        listBudget.add(b1);
        listBudget.add(b2);

        given(proposalRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).willReturn(listProposal);
        given(budgetRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).willReturn(listBudget);
        given(junctionRepository.findById(1L)).willReturn(java.util.Optional.of(junction));
        given(apiConnectRepository.getExpired(false)).willReturn(connect);

        mockMvc.perform(get("/proposalbudget/edit/{url}", "proposal-1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("apiConnect", connect))
                .andExpect(model().attribute("listProposal", hasSize(2)))
                .andExpect(model().attribute("listBudget", hasSize(2)))
                .andExpect(view().name("proposal_budget/edit"));
    }

    @Test
    public void whenGetEditJunction_thenReturnFalseTryCatch() throws Exception {

        when(junctionRepository.findById(1L)).thenThrow(new ArrayIndexOutOfBoundsException());

        mockMvc.perform(get("/proposalbudget/edit/{url}", "proposal-1"))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposal"));
    }

    @Test
    public void whenPostUpdateJunction_thenReturnSuccess() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        Proposal proposal = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        proposal.setId(id);

        ApiConnect apiConnect = new ApiConnect("test", "test", false);

        JunctionWrapper junction = new JunctionWrapper();
        junction.setId(id);
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setAmount("1");
        junction.setSfid(sfid);
        junction.setTypeRedirect("proposal");
        junction.setIdRedirect("1");

        Junction newJunction = new Junction(junction);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Budget__c", junction.getBudgetId());
        dataJson.put("Proposal__c", junction.getProposalId());
        dataJson.put("Amount__c", junction.getAmount());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.patchRestTemplate(URL_CREATE_JUNCTION+"/"+junction.getSfid(), apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        given(proposalRepository.findBySfid("Test1")).willReturn(proposal);

        mockMvc.perform(
                post("/proposalbudget/update/{id}", 1).flashAttr("junction", junction).with(csrf())
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposal/1"));
    }

    @Test
    public void whenPostUpdateJunction_thenReturnFalseFlagUpdate() throws Exception {
        Long id = 1L;
        String sfid = "test123";

        JunctionWrapper junction = new JunctionWrapper();
        junction.setId(id);
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setAmount("1");
        junction.setSfid(sfid);
        junction.setTypeRedirect("proposal");
        junction.setIdRedirect("1");

        given(apiConnectRepository.getExpired(false)).willReturn(null);

        mockMvc.perform(
                post("/proposalbudget/update/{id}", 1).flashAttr("junction", junction).with(csrf())
        ).andExpect(status().isFound())
                .andExpect(view().name("redirect:/proposalbudget/edit/proposal-1"));
    }

    @Test
    public void whenPostUpdateJunction_thenReturnFalseTryCatch() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        Proposal proposal = new Proposal("Test1", "2020-02-02 12:00:00", "2020-02-02 12:00:00", "2020", "", 0.0, "test1");
        proposal.setId(id);

        ApiConnect apiConnect = new ApiConnect("test", "test", false);

        JunctionWrapper junction = new JunctionWrapper();
        junction.setId(id);
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setAmount("1");
        junction.setSfid(sfid);
        junction.setTypeRedirect("proposal");
        junction.setIdRedirect("1");

        Junction newJunction = new Junction(junction);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);

        JSONObject dataJson = new JSONObject();
        dataJson.put("Budget__c", junction.getBudgetId());
        dataJson.put("Proposal__c", junction.getProposalId());
        dataJson.put("Amount__c", junction.getAmount());

        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.patchRestTemplate(URL_CREATE_JUNCTION+"/"+junction.getSfid(), apiConnect.getToken(), dataJson.toString())).willReturn(responseStatus);
        when(proposalRepository.findBySfid("Test1")).thenThrow(new ArrayIndexOutOfBoundsException());

        mockMvc.perform(
                post("/proposalbudget/update/{id}", 1).flashAttr("junction", junction).with(csrf())
        ).andExpect(status().isFound())
         .andExpect(view().name("redirect:/proposalbudget/edit/proposal-1"));
    }

    @Test
    public void whenGetDeleteJunction_thenReturnSuccess() throws Exception {

        Long id = 1L;
        String sfid = "test123";
        ApiConnect apiConnect = new ApiConnect("test", "test", false);
        Junction junction = new Junction();
        junction.setId(id);
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setSfid(sfid);
        junction.setAmount(0.0);

        ResponseStatus responseStatus = new ResponseStatus();
        responseStatus.setStatusCode(200);
        responseStatus.setSuccess(true);

        given(junctionRepository.findById(1L)).willReturn(Optional.of(junction));
        given(apiConnectRepository.getExpired(false)).willReturn(apiConnect);
        given(utilCallApi.deleteRestTemplate(URL_CREATE_JUNCTION+"/"+junction.getSfid(), apiConnect.getToken())).willReturn(responseStatus);

        mockMvc.perform(
                delete("/proposalbudget/destroy/{id}", 1)
        ).andExpect(status().isOk())
         .andExpect(content().string("SUCCESS"));
    }

    @Test
    public void whenGetDeleteJunction_thenReturnFalseFlagDelete() throws Exception {
        Long id = 1L;
        String sfid = "test123";

        Junction junction = new Junction();
        junction.setId(id);
        junction.setProposalId("Test1");
        junction.setBudgetId("Test1");
        junction.setSfid(sfid);
        junction.setAmount(0.0);

        given(junctionRepository.findById(1L)).willReturn(Optional.of(junction));
        given(apiConnectRepository.getExpired(false)).willReturn(null);

        mockMvc.perform(
                delete("/proposalbudget/destroy/{id}", 1)
        ).andExpect(status().isOk())
                .andExpect(content().string("FALSE"));
    }
}
