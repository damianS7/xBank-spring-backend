package com.damian.xBank.banking.account;

import com.damian.xBank.banking.transactions.BankingAccountTransaction;
import com.damian.xBank.common.DTOBuilder;
import com.damian.xBank.customer.Customer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "banking_accounts")
public class BankingAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "ownerAccount", cascade = CascadeType.ALL)
    private Set<BankingAccountTransaction> accountTransactions;

    @Column(length = 32, nullable = false)
    private String accountNumber;

    @Column(precision = 15, scale = 3)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private BankingAccountType accountType;

    @Enumerated(EnumType.STRING)
    private BankingAccountCurrency accountCurrency;

    @Enumerated(EnumType.STRING)
    private BankingAccountStatus accountStatus;

    private Instant createdAt;

    public BankingAccount() {
        this.accountTransactions = new HashSet<>();
        this.balance = BigDecimal.valueOf(0);
        this.accountStatus = BankingAccountStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public BankingAccount(Customer customer) {
        this();
        this.customer = customer;
        this.customer.addBankingAccount(this);
    }

    public BankingAccount(String accountNumber,
                          BankingAccountType accountType,
                          BankingAccountCurrency accountCurrency) {
        this();
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.accountCurrency = accountCurrency;
    }

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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String number) {
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

    public BankingAccountCurrency getAccountCurrency() {
        return accountCurrency;
    }

    public void setAccountCurrency(BankingAccountCurrency accountCurrency) {
        this.accountCurrency = accountCurrency;
    }

    public BankingAccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(BankingAccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Set<BankingAccountTransaction> getAccountTransactions() {
        return accountTransactions;
    }

    public void setAccountTransactions(Set<BankingAccountTransaction> accountTransactions) {
        this.accountTransactions = accountTransactions;
    }

    public void addAccountTransaction(BankingAccountTransaction transaction) {
        transaction.setOwnerAccount(this);
        this.accountTransactions.add(transaction);
    }
}
