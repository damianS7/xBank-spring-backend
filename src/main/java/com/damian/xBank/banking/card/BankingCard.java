package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.customer.Customer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "banking_cards")
public class BankingCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "banking_account_id", referencedColumnName = "id", nullable = false)
    private BankingAccount bankingAccount;

    @Column(length = 20, nullable = false)
    private String cardNumber;

    @Column(precision = 15, scale = 2)
    private BigDecimal dailyLimit;

    @Enumerated(EnumType.STRING)
    private BankingCardType cardType;

    @Column(length = 3)
    private String cardCvv;

    @Column(length = 4)
    private String cardPin;

    @Column
    private LocalDate expiredDate;

    @Enumerated(EnumType.STRING)
    private BankingCardStatus cardStatus;

    @Enumerated(EnumType.STRING)
    private BankingCardLockStatus lockStatus;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public BankingCard() {
        this.cardStatus = BankingCardStatus.ENABLED;
        this.cardType = BankingCardType.DEBIT;
        this.lockStatus = BankingCardLockStatus.UNLOCKED;
        this.dailyLimit = BigDecimal.valueOf(3000);
    }

    public BankingCard(BankingAccount bankingAccount) {
        this();
        this.bankingAccount = bankingAccount;
    }

    public BankingCard(BankingAccount bankingAccount, String cardNumber, BankingCardType cardType) {
        this();
        this.bankingAccount = bankingAccount;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCardOwner() {
        return bankingAccount.getOwner();
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String number) {
        this.cardNumber = number;
    }

    public BankingCardType getCardType() {
        return cardType;
    }

    public void setCardType(BankingCardType cardType) {
        this.cardType = cardType;
    }

    public BankingCardStatus getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(BankingCardStatus cardStatus) {
        this.cardStatus = cardStatus;
    }

    public BankingAccount getAssociatedBankingAccount() {
        return bankingAccount;
    }

    public void setAssociatedBankingAccount(BankingAccount bankingAccount) {
        this.bankingAccount = bankingAccount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getCardCvv() {
        return cardCvv;
    }

    public void setCardCvv(String CVV) {
        this.cardCvv = CVV;
    }

    public String getCardPin() {
        return cardPin;
    }

    public void setCardPin(String cardPin) {
        this.cardPin = cardPin;
    }

    public BankingCardLockStatus getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(BankingCardLockStatus lockStatus) {
        this.lockStatus = lockStatus;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getBalance() {
        return this.getAssociatedBankingAccount().getBalance();
    }

    // returns true if the operation can be carried
    public boolean hasEnoughFundsToSpend(BigDecimal amount) {
        // if its 0 then balance is equal to the amount willing to spend
        // if its 1 then balance is greater than the amount willing to spend
        return this.getAssociatedBankingAccount().hasEnoughFunds(amount);
    }

    public BigDecimal chargeAmount(BigDecimal amount) {
        return this.getAssociatedBankingAccount().subtractAmount(amount);
    }

    public String getHolderName() {
        return this.getAssociatedBankingAccount().getOwner().getFullName();
    }
}
