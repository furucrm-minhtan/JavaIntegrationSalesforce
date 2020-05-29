package com.citron.javaintegrationsalesforce.repository;

import com.citron.javaintegrationsalesforce.model.Proposal;
import com.citron.javaintegrationsalesforce.model.ProposalBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    Proposal findBySfid(String sfid);
}
