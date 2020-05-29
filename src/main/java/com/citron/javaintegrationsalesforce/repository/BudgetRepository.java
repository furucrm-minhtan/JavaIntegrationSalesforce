package com.citron.javaintegrationsalesforce.repository;


import com.citron.javaintegrationsalesforce.model.Budget;
import com.citron.javaintegrationsalesforce.model.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Budget findBySfid(String sfid);
}
