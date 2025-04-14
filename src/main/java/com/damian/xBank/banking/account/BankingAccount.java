package com.damian.xBank.banking.account;

import com.damian.xBank.common.DTOBuilder;
import com.damian.xBank.customer.Customer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "banking_accounts")
public class BankingAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @Column(length = 20, nullable = false)
    private String accountNumber;

    @Column(precision = 15, scale = 3)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private BankingAccountType accountType;

    @Enumerated(EnumType.STRING)
    private BankingAccountCurrency currency;

    @Enumerated(EnumType.STRING)
    private BankingAccountStatus status;

    private Instant updatedAt;

    public BankingAccountDTO toDTO() {
        return DTOBuilder.build(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getNumber() {
        return accountNumber;
    }

    public void setNumber(String number) {
        this.accountNumber = number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BankingAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(BankingAccountType accountType) {
        this.accountType = accountType;
    }

    public BankingAccountCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(BankingAccountCurrency currency) {
        this.currency = currency;
    }

    public BankingAccountStatus getStatus() {
        return status;
    }

    public void setStatus(BankingAccountStatus status) {
        this.status = status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
