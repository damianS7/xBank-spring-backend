package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.common.utils.DTOMapper;
import com.damian.xBank.customer.Customer;
import jakarta.persistence.*;

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
        return bankingAccount.getCustomer();
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

    public BankingAccount getLinkedBankingAccount() {
        return bankingAccount;
    }

    public void setLinkedBankingAccount(BankingAccount bankingAccount) {
        this.bankingAccount = bankingAccount;
    }
}
