package com.citron.javaintegrationsalesforce.model;

import javax.validation.constraints.Size;

public class JunctionWrapper {
    private long id;
    private String proposalId;
    private String budgetId;
    @Size(min = 0, max = 18)
    private String amount;
    private String sfid;
    private String typeRedirect;
    private String idRedirect;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSfid() {
        return sfid;
    }

    public void setSfid(String sfid) {
        this.sfid = sfid;
    }

    public String getTypeRedirect() {
        return typeRedirect;
    }

    public void setTypeRedirect(String typeRedirect) {
        this.typeRedirect = typeRedirect;
    }

    public String getIdRedirect() {
        return idRedirect;
    }

    public void setIdRedirect(String idRedirect) {
        this.idRedirect = idRedirect;
    }
}
