package com.citron.javaintegrationsalesforce.model;

import javax.persistence.*;

@Entity
@Table(name = "proposal_budget__c")
public class ProposalBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "proposal__c", nullable = false, length = 80)
    private String proposalId;
    @Column(name = "budget__c", nullable = false, length = 80)
    private String budgetId;
    @Column(name = "amount__c")
    private Double amount;
    @Column(name = "sfid")
    private String sfid;
    private String name;
    private String budgetlink;

    public ProposalBudget(){

    }

    public ProposalBudget(String proposalId, String budgetId, Double amount, String sfid){
        this.proposalId = proposalId;
        this.budgetId = budgetId;
        this.amount = amount;
        this.sfid = sfid;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return  this.id;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getProposalId() {
        return this.proposalId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public String getBudgetId() {
        return this.budgetId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return this.amount;
    }

    public void setSfid(String sfid) { this.sfid = sfid; }

    public String getSfid() { return this.sfid; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getBudgetLink() {
        return budgetlink;
    }

    public void setBudgetLink(String budgetlink) {
        this.budgetlink = budgetlink;
    }
}
