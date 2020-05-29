package com.citron.javaintegrationsalesforce.model;

import com.citron.javaintegrationsalesforce.util.UtilClass;

import javax.persistence.*;

@Entity
@Table(name = "proposal_budget__c")
public class Junction {

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

    public Junction(){

    }

    public Junction(JunctionWrapper jt) {
        this.id = jt.getId();
        this.proposalId = jt.getProposalId();
        this.budgetId = jt.getBudgetId();
        this.amount = Double.parseDouble(jt.getAmount());
        this.sfid = jt.getSfid();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSfid() {
        return sfid;
    }

    public void setSfid(String sfid) {
        this.sfid = sfid;
    }
}
