package com.citron.javaintegrationsalesforce.services;

import com.citron.javaintegrationsalesforce.model.Budget;
import com.citron.javaintegrationsalesforce.util.exception.CallApiException;
import org.json.JSONException;

import java.util.List;

public interface BudgetService {
    public abstract List<Budget> getAllBudget();
    public abstract Budget getBudgetById(Long id);
    public abstract Long createBudget(Budget budget) throws Exception;
    public abstract Boolean updateBudget(Budget budget) throws Exception;
    public abstract Boolean deleteBudget(Long id) throws Exception;
}
