package com.citron.javaintegrationsalesforce.model;

import com.citron.javaintegrationsalesforce.util.UtilClass;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.w3c.dom.Text;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "proposal__c")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name", nullable = false, length = 80)
    private String name;
    @Column(name = "proposed_at__c", nullable = false)
    private LocalDateTime proposedAt;
    @Column(name = "approved_at__c", nullable =  false)
    private LocalDateTime approvedAt;
    @Column(name="details__c")
    private String detail;
    @Column(name = "year__c", nullable = false, length = 4)
    private String year;
    @Column(name = "total_amount__c")
    private Double totalAmount;
    @Column(name = "sfid")
    private String sfid;

    public Proposal(){

    }

    public Proposal(ProposalWrapper pw){
        this.id = pw.getId();
        this.name = pw.getName();
        this.year = pw.getYear();
        this.detail = pw.getDetail();
        this.sfid = pw.getSfid();
        this.approvedAt = convertDateTimeInsert(pw.getApprovedAt());
        this.proposedAt = convertDateTimeInsert(pw.getProposedAt());
        this.totalAmount = pw.getTotalAmount();
    }

    public Proposal(String name, String proposedAt, String approvedAt, String year, String detail, Double totalAmount, String sfid) {
        this.name = name;
        this.year = year;
        this.detail = detail;
        this.sfid = sfid;
        this.approvedAt = convertDateTimeInsert(approvedAt);
        this.proposedAt = convertDateTimeInsert(proposedAt);
        this.totalAmount = totalAmount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return  this.id;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getYear() {
        return this.year;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getTotalAmount() {
        return this.totalAmount;
    }

    public void setProposedAt(LocalDateTime proposedAt) { this.proposedAt = proposedAt;}

    public LocalDateTime getProposedAt() { return  this.proposedAt;}

    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getApprovedAt() { return this.approvedAt; }

    public void setDetail(String detail) { this.detail = detail; }

    public String getDetail() {return this.detail; }

    public void setSfid(String sfid) { this.sfid = sfid; }

    public String getSfid() {return this.sfid; }

    public LocalDateTime convertDateTimeInsert(String datetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(datetime, formatter).minusHours(9);
        return dateTime;
    }

    public String getFormatProposedAt() {
        LocalDateTime datetimeJP = this.proposedAt.plusHours(9);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = datetimeJP.format(formatter);
        return formattedDateTime;
    }

    public String getFormatApprovedAt() {
        LocalDateTime datetimeJP = this.approvedAt.plusHours(9);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = datetimeJP.format(formatter);
        return formattedDateTime;
    }
}
