package com.damian.xBank.banking.account.transactions;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.common.utils.DTOBuilder;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "banking_account_transactions")
public class BankingAccountTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "banking_account_id", referencedColumnName = "id", nullable = false)
    private BankingAccount ownerAccount;

    @Column(precision = 15, scale = 3)
    private BigDecimal amount;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    private BankingAccountTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private BankingAccountTransactionStatus transactionStatus;

    private Instant createdAt;

    public BankingAccountTransaction(BankingAccount ownerAccount) {
        this();
        this.ownerAccount = ownerAccount;
    }

    public BankingAccountTransaction() {
        this.amount = BigDecimal.valueOf(0);
        this.transactionStatus = BankingAccountTransactionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public BankingAccountTransaction(BankingAccountTransactionType transactionType) {
        this();
        this.transactionType = transactionType;
    }

    public BankingAccountTransactionDTO toDTO() {
        return DTOBuilder.build(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankingAccount getOwnerAccount() {
        return ownerAccount;
    }

    public void setOwnerAccount(BankingAccount account) {
        this.ownerAccount = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BankingAccountTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(BankingAccountTransactionType transactionType) {
        this.transactionType = transactionType;
    }


    public BankingAccountTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(BankingAccountTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
