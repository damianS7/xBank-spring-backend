package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.common.utils.DTOMapper;
import com.damian.xBank.customer.Customer;
import jakarta.persistence.*;

import java.time.Instant;

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

    @Enumerated(EnumType.STRING)
    private BankingCardType cardType;

    @Enumerated(EnumType.STRING)
    private BankingCardStatus cardStatus;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    public BankingCard() {
        this.cardStatus = BankingCardStatus.ENABLED;
        this.cardType = BankingCardType.DEBIT;
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

    public BankingCardDTO toDTO() {
        return DTOMapper.build(this);
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
}
