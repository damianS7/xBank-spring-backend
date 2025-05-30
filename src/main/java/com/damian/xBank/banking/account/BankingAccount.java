package com.damian.xBank.banking.account;

import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.transactions.BankingTransaction;
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

    // FIXME switch to LAZY?
    @OneToMany(mappedBy = "bankingAccount", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<BankingTransaction> accountTransactions;

    // FIXME switch to LAZY?
    @OneToMany(mappedBy = "bankingAccount", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<BankingCard> bankingCards;

    @Column(length = 64)
    private String alias;

    @Column(length = 32, nullable = false)
    private String accountNumber;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private BankingAccountType accountType;

    @Enumerated(EnumType.STRING)
    private BankingAccountCurrency accountCurrency;

    @Enumerated(EnumType.STRING)
    private BankingAccountStatus accountStatus;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public BankingAccount() {
        this.accountTransactions = new HashSet<>();
        this.bankingCards = new HashSet<>();
        this.balance = BigDecimal.valueOf(0);
        this.accountType = BankingAccountType.SAVINGS;
        this.accountStatus = BankingAccountStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public BankingAccount(Customer customer) {
        this();
        this.customer = customer;
        this.customer.addBankingAccount(this);
    }

    public BankingAccount(
            String accountNumber,
            BankingAccountType accountType,
            BankingAccountCurrency accountCurrency
    ) {
        this();
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.accountCurrency = accountCurrency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getOwner() {
        return customer;
    }

    public void setOwner(Customer customer) {
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

    public Set<BankingTransaction> getAccountTransactions() {
        return accountTransactions;
    }

    public void setAccountTransactions(Set<BankingTransaction> accountTransactions) {
        this.accountTransactions = accountTransactions;
    }

    public void addAccountTransaction(BankingTransaction transaction) {
        if (transaction.getAssociatedBankingAccount() != this) {
            transaction.setAssociatedBankingAccount(this);
        }
        this.accountTransactions.add(transaction);
    }

    public Set<BankingCard> getBankingCards() {
        return this.bankingCards;
    }

    public void addBankingCard(BankingCard bankingCard) {
        if (bankingCard.getAssociatedBankingAccount() != this) {
            bankingCard.setAssociatedBankingAccount(this);
        }
        this.bankingCards.add(bankingCard);
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    // returns true if the operation can be carried
    public boolean hasEnoughFunds(BigDecimal amount) {
        // if its 0 then balance is equal to the amount willing to spend
        // if its 1 then balance is greater than the amount willing to spend
        return this.getBalance().compareTo(amount) >= 0;
    }

    public BigDecimal subtractAmount(BigDecimal amount) {
        if (this.hasEnoughFunds(amount)) {
            this.setBalance(this.getBalance().subtract(amount));
        }
        return this.getBalance();
    }

    public BigDecimal addAmount(BigDecimal amount) {
        this.setBalance(this.getBalance().add(amount));
        return this.getBalance();
    }
}
