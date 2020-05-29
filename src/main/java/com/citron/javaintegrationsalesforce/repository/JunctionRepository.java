package com.citron.javaintegrationsalesforce.repository;

import com.citron.javaintegrationsalesforce.model.Junction;
import com.citron.javaintegrationsalesforce.model.ProposalBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JunctionRepository extends JpaRepository<Junction, Long> {
}
