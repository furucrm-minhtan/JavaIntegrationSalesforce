package com.citron.javaintegrationsalesforce.model;


import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Entity
@Table(name = "budget__c")
public class Budget  {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotBlank
    @Size(min = 1, max = 80)
    @Column(name = "name", nullable = false, length = 80)
    private String name;
    @NotBlank
    @Column(name = "year__c", nullable = false, length = 4)
    private String year;
    @Column(name = "total_amount__c", nullable = false, columnDefinition = "Double default 0.0")
    private Double totalAmount;
    @Column(name = "sfid")
    private String sfid;

    public Budget(){}

    public Budget(String name, String year) {
        this.name = name;
        this.year = year;
        this.totalAmount = 0.0;
    }

    public Budget(String name, String year, Double totalAmount) {
        this.name = name;
        this.year = year;
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

    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getTotalAmount() {
        return this.totalAmount;
    }

    public void setSfid(String sfid) {
        this.sfid = sfid;
    }

    public String getSfid() {
        return this.sfid;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
