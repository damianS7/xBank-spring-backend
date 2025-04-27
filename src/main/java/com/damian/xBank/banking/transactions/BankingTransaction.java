package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.common.utils.DTOBuilder;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "banking_account_transactions")
public class BankingTransaction {
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
    private BankingTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private BankingTransactionStatus transactionStatus;

    private Instant createdAt;

    public BankingTransaction(BankingAccount ownerAccount) {
        this();
        this.ownerAccount = ownerAccount;
    }

    public BankingTransaction() {
        this.amount = BigDecimal.valueOf(0);
        this.transactionStatus = BankingTransactionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public BankingTransaction(BankingTransactionType transactionType) {
        this();
        this.transactionType = transactionType;
    }

    public BankingTransactionDTO toDTO() {
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

    public BankingTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(BankingTransactionType transactionType) {
        this.transactionType = transactionType;
    }


    public BankingTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(BankingTransactionStatus transactionStatus) {
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
