package com.citron.javaintegrationsalesforce.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProposalWrapper {
    private long id;
    @NotBlank
    @Size(max=80)
    private String name;
    private String proposedAt;
    private String approvedAt;
    private String year;
    private Double totalAmount;
    private String detail;
    private String sfid;

    public ProposalWrapper(){

    }

    public ProposalWrapper(String name, String proposedAt, String approvedAt, String year, Double totalAmount, String detail, String sfid) {
        this.name = name;
        this.proposedAt = proposedAt;
        this.approvedAt = approvedAt;
        this.year = year;
        this.totalAmount = totalAmount;
        this.detail = detail;
        this.sfid = sfid;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getProposedAt() { return proposedAt; }
    public void setProposedAt(String proposedAt) { this.proposedAt = proposedAt; }

    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getSfid() { return sfid; }
    public void setSfid(String sfid) { this.sfid = sfid; }

}
