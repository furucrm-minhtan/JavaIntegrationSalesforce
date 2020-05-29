package com.citron.javaintegrationsalesforce.controller;

import com.citron.javaintegrationsalesforce.model.Budget;
import com.citron.javaintegrationsalesforce.services.BudgetServiceImp;
import com.citron.javaintegrationsalesforce.util.exception.CallApiException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.logging.Logger;


@Controller
public class BudgetController {
    private final static java.util.logging.Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private BudgetServiceImp budgetServiceImp;

    public BudgetController(BudgetServiceImp _budgetServiceImp) {
        this.budgetServiceImp = _budgetServiceImp;
    }

    @RequestMapping(path = "/budget", method = RequestMethod.GET)
    public String index(Model model) {
        try {
            model.addAttribute("budgetCollection", budgetServiceImp.getAllBudget());
            return "budget/list";
        }
        catch (Exception ex) {
        }
        return null;
    }

    @RequestMapping(path = "/budget/{id}", method = RequestMethod.GET)
    public String show(Model model, @PathVariable("id") Long id) {
        try{
            Budget budget = budgetServiceImp.getBudgetById(id);
            model.addAttribute("budget", budget);
            model.addAttribute("listProposalBudget", budgetServiceImp.getJunction(budget.getSfid()));
            return "budget/show";
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
            logger.warning(ex.getMessage());
            logger.info(this.getClass().getName()+": "+ex.getCause());
        }
        return null;
    }

    @RequestMapping(path = "budget/create", method = RequestMethod.GET)
    public String create(Model model){
        model.addAttribute("budget", new Budget());
        model.addAttribute("apiConnect", budgetServiceImp.getApiConnect().getExpired(false));
        return "budget/create";
    }

    @RequestMapping(path = "budget/create",method = RequestMethod.POST)
    public String store(@Valid @ModelAttribute("budget") Budget budget, BindingResult bindingresult, Model model) {
        try {
            if (!bindingresult.hasErrors()) {
                Long id =budgetServiceImp.createBudget(budget);
                if( id > 0L) {
                    return "redirect:/budget/"+id;
                }
            }
        }
        catch (Exception ex) {
            //bindingresult.reject("error", ex.getMessage());
        }
        model.addAttribute("apiConnect", budgetServiceImp.getApiConnect().getExpired(false));
        return "budget/create";
    }

    @RequestMapping(path = "/budget/edit/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") Long id, Model model) {
       try {
           model.addAttribute("budget", budgetServiceImp.getBudgetById(id) );
           model.addAttribute("apiConnect", budgetServiceImp.getApiConnect().getExpired(false));
           return "budget/edit";
       }
       catch (Exception ex) {
           System.out.println(ex.getMessage());
           logger.warning(ex.getMessage());
           logger.info(this.getClass().getName()+": "+ex.getCause());
       }
       return null;
    }

    @RequestMapping(path = "/budget/edit/{id}", method = {RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT})
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute("budget") Budget budget, BindingResult bindingresult, Model model) {
        try {
            if (!bindingresult.hasErrors()) {
                Boolean updated = budgetServiceImp.updateBudget(budget);
                if(updated) {
                    return  "redirect:/budget/"+budget.getId();
                }
            }
        }
        catch (Exception ex) {
            //bindingresult.reject("error", ex.getMessage());
        }
        model.addAttribute("apiConnect", budgetServiceImp.getApiConnect().getExpired(false));
        return "budget/edit";
    }

    @RequestMapping(path = "/budget/destroy/{id}", method = {RequestMethod.GET})
    public String destroy(@PathVariable("id") Long id) {
        try {
            budgetServiceImp.deleteBudget(id);
        }
        catch (Exception ex) {

        }
        return "redirect:/budget";
    }
}
