package com.citron.javaintegrationsalesforce;

import com.citron.javaintegrationsalesforce.controller.BudgetController;
import com.citron.javaintegrationsalesforce.model.ApiConnect;
import com.citron.javaintegrationsalesforce.model.Budget;
import com.citron.javaintegrationsalesforce.repository.ApiConnectRepository;
import com.citron.javaintegrationsalesforce.services.BudgetServiceImp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
public class BudgetControllerTest {


    private MockMvc mockMvc;

    private BudgetController budgetController;

    @Mock
    private BudgetServiceImp budgetServiceImp;

    @Mock
    private ApiConnectRepository apiConnectRepository;

    @Before
    public void setup() {
        budgetController = new BudgetController(budgetServiceImp);
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();
    }

    @Test
    public void showAllBudgetInListPageTest() throws Exception {
        ArrayList<Budget> list = new ArrayList<>();
        Budget budget1 = new Budget("Test1", "2001");
        Budget budget2 = new Budget("Test2", "2002");
        Budget budget3 = new Budget("Test3", "2003");

        list.add(budget1);
        list.add(budget2);
        list.add(budget3);

        given(budgetServiceImp.getAllBudget()).willReturn(list);

        HttpServletResponse response = mockMvc.perform(get("/budget"))
                .andExpect(view().name("budget/list"))
                .andExpect(model().attribute("budgetCollection", list))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void redirectToShowPageTest() throws Exception {
        Budget budget1 = new Budget("Test1", "2001");

        given(budgetServiceImp.getBudgetById(1L)).willReturn(budget1);

        HttpServletResponse response = mockMvc.perform(get("/budget/{id}", 1))
                .andExpect(view().name("budget/show"))
                .andExpect(model().attribute("budget", budget1))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void redirectCreatePageTest() throws Exception {
        ApiConnect connect = new ApiConnect("test", "test", false);
        given(budgetServiceImp.getApiConnect()).willReturn(apiConnectRepository);
        given(budgetServiceImp.getApiConnect().getExpired(false)).willReturn(connect);

        HttpServletResponse response = mockMvc.perform(get("/budget/create"))
                .andExpect(view().name("budget/create"))
                .andExpect(model().attribute("apiConnect", connect))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void redirectEditPageTest() throws Exception {
        Budget budget1 = new Budget("Test1", "2001");
        budget1.setId(1L);
        ApiConnect connect = new ApiConnect("test", "test", false);
        given(budgetServiceImp.getBudgetById(1L)).willReturn(budget1);
        given(budgetServiceImp.getApiConnect()).willReturn(apiConnectRepository);
        given(budgetServiceImp.getApiConnect().getExpired(false)).willReturn(connect);

        HttpServletResponse response = mockMvc.perform(get("/budget/edit/{id}", 1))
                .andExpect(view().name("budget/edit"))
                .andExpect(model().attribute("budget",budget1))
                .andExpect(model().attribute("apiConnect", connect))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void whenRequestStoreToCreateBudget_thenRedirectToDetailPageIfSuccess() throws Exception {
        Budget budget = new Budget("Test1", "2001");
        given(budgetServiceImp.createBudget(budget)).willReturn(1L);

        HttpServletResponse response = mockMvc.perform(
                    post("/budget/create")
                    .flashAttr("budget", budget)
                    .with(csrf())
                )
                .andExpect(model().attributeHasNoErrors("budget"))
                .andExpect(view().name("redirect:/budget/1"))
                .andReturn().getResponse();


        assertThat(HttpStatus.valueOf(response.getStatus())).isEqualTo(HttpStatus.FOUND);
    }

    @Test
    public void whenRequestStoreToCreateBudget_thenRedirectToCreatePageIfFail() throws Exception {
        Budget budget = new Budget("Test1", "");
        ApiConnect connect = new ApiConnect("test", "test", false);
        given(budgetServiceImp.createBudget(budget)).willReturn(1L);
        given(budgetServiceImp.getApiConnect()).willReturn(apiConnectRepository);
        given(budgetServiceImp.getApiConnect().getExpired(false)).willReturn(connect);
        HttpServletResponse response = mockMvc.perform(
                post("/budget/create")
                        .flashAttr("budget", budget)
                        .with(csrf())
                )
                .andExpect(model().attributeHasErrors("budget"))
                .andExpect(view().name("budget/create"))
                .andExpect(model().attribute("apiConnect", connect))
                .andReturn().getResponse();

        assertThat(HttpStatus.valueOf(response.getStatus())).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void whenRequestUpdateToUpdateBudget_thenRedirectToDetailPageIfSuccess() throws Exception {
        Budget budget = new Budget("Test1123", "2000");
        budget.setId(1L);
        given(budgetServiceImp.updateBudget(budget)).willReturn(true);

        HttpServletResponse response = mockMvc.perform(
                put("/budget/edit/{id}", 1L)
                        .flashAttr("budget", budget)
                        .secure(true)
                )
                .andDo(print())
                .andExpect(model().attributeHasNoErrors("budget"))
                .andExpect(view().name("redirect:/budget/"+budget.getId()))
                .andReturn().getResponse();

        assertThat(HttpStatus.valueOf(response.getStatus())).isEqualTo(HttpStatus.FOUND);
    }

    @Test
    public void whenRequestUpdateToUpdateBudget_thenRedirectToCreatePageIfFail() throws Exception {
        Budget budget = new Budget("Test1", "");
        budget.setId(1L);
        ApiConnect connect = new ApiConnect("test", "test", false);
        given(budgetServiceImp.updateBudget(budget)).willReturn(false);
        given(budgetServiceImp.getApiConnect()).willReturn(apiConnectRepository);
        given(budgetServiceImp.getApiConnect().getExpired(false)).willReturn(connect);
        HttpServletResponse response = mockMvc.perform(
                put("/budget/edit/{id}", 1)
                        .flashAttr("budget", budget)
                )
                .andExpect(model().attributeHasErrors("budget"))
                .andExpect(view().name("budget/edit"))
                .andExpect(model().attribute("apiConnect", connect))
                .andReturn().getResponse();

        assertThat(HttpStatus.valueOf(response.getStatus())).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void whenRequestDestroyToDeleteBudget_thenRedirectToHomeBudgetPageIfSuccess() throws Exception {
        given(budgetServiceImp.deleteBudget(1L)).willReturn(true);

        HttpServletResponse response = mockMvc.perform(
                get("/budget/destroy/{id}", 1L))
                .andExpect(view().name("redirect:/budget"))
                .andReturn().getResponse();
        assertThat(HttpStatus.valueOf(response.getStatus())).isEqualTo(HttpStatus.FOUND);
    }
}
