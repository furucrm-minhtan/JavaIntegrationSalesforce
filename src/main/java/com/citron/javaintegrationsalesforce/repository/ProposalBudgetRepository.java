package com.citron.javaintegrationsalesforce.repository;

import com.citron.javaintegrationsalesforce.model.ProposalBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalBudgetRepository extends JpaRepository<ProposalBudget, Long> {
    List<ProposalBudget> findByProposalId(String proposalId);
    //List<ProposalBudget> findByBudgetId(String budgetId);

    @Query(value = "SELECT b.name, b.id as budgetlink, j.* FROM proposal_budget__c j INNER JOIN budget__c b ON j.budget__c = b.sfid WHERE j.proposal__c = :proposalId", nativeQuery = true)
    List<ProposalBudget> getJunctionFromProposal(@Param("proposalId") String proposalId);

    @Query(value = "SELECT p.name, p.id as budgetlink, j.* FROM proposal_budget__c j INNER JOIN proposal__c p ON j.proposal__c = p.sfid WHERE j.budget__c = :budgetId", nativeQuery = true)
    List<ProposalBudget> getJunctionFromBudget(@Param("budgetId") String budgetId);
}
