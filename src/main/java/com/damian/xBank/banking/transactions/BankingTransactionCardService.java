package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardAuthorizationHelper;
import com.damian.xBank.banking.card.BankingCardRepository;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionException;
import com.damian.xBank.banking.transactions.http.BankingCardTransactionRequest;
import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.common.utils.AuthHelper;
import com.damian.xBank.customer.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankingTransactionCardService {

    private final BankingCardRepository bankingCardRepository;
    private final BankingTransactionService bankingTransactionService;

    public BankingTransactionCardService(
            BankingCardRepository bankingCardRepository,
            BankingTransactionService bankingTransactionService
    ) {
        this.bankingCardRepository = bankingCardRepository;
        this.bankingTransactionService = bankingTransactionService;
    }

    // handle request BankingTransactionType and determine what to do.
    public BankingTransaction processTransactionRequest(
            Long cardId,
            BankingCardTransactionRequest request
    ) {
        // BankingCard to operate
        BankingCard bankingCard = bankingCardRepository.findById(cardId).orElseThrow(
                () -> new BankingCardNotFoundException(
                        Exceptions.CARD.NOT_FOUND
                )
        );

        return switch (request.transactionType()) {
            case CARD_CHARGE -> this.spend(bankingCard, request.cardPin(), request.amount(), request.description());
            case WITHDRAWAL -> this.withdrawal(bankingCard, request.cardPin(), request.amount());
            default -> throw new BankingTransactionException(
                    Exceptions.TRANSACTION.INVALID_TYPE
            );
        };
    }

    private void canCarryOperationOrElseThrow(
            BankingCard card,
            Customer customerLogged,
            String cardPIN,
            BigDecimal amount
    ) {
        BankingCardAuthorizationHelper
                .authorize(customerLogged, card)
                // check customer authorization
                .checkOwner()
                // check if card is not disabled or locked
                .checkStatus()
                .checkPIN(cardPIN);

        // check balance
        this.checkFunds(card, amount);
    }

    public void checkFunds(BankingCard card, BigDecimal amount) {
        if (!card.hasEnoughFundsToSpend(amount)) {
            throw new BankingCardAuthorizationException(
                    Exceptions.CARD.INSUFFICIENT_FUNDS
            );
        }
    }

    // validates card status and does the transaction
    public BankingTransaction spend(
            BankingCard card,
            String cardPIN,
            BigDecimal amount,
            String description
    ) {
        BankingTransaction transaction = this.bankingTransactionService.createTransaction(
                card,
                BankingTransactionType.CARD_CHARGE,
                amount,
                description
        );
        final Customer customerLogged = AuthHelper.getLoggedCustomer();

        // run validations and throw if any throw exception
        this.canCarryOperationOrElseThrow(card, customerLogged, cardPIN, amount);

        // if the transaction is created, deduce the amount from balance
        card.chargeAmount(amount);

        // transaction is completed
        transaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);

        // save the transaction
        return bankingTransactionService.persistTransaction(transaction);
    }

    // withdraws money
    public BankingTransaction withdrawal(
            BankingCard card,
            String cardPIN,
            BigDecimal amount
    ) {
        BankingTransaction transaction = this.bankingTransactionService.createTransaction(
                card,
                BankingTransactionType.WITHDRAWAL,
                amount,
                "ATM withdrawal."
        );

        final Customer customerLogged = AuthHelper.getLoggedCustomer();
        this.canCarryOperationOrElseThrow(card, customerLogged, cardPIN, amount);

        // check balance
        this.checkFunds(card, amount);

        // if the transaction is created, deduce the amount from balance
        card.chargeAmount(amount);

        // transaction is completed
        transaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);

        // save the transaction
        return bankingTransactionService.persistTransaction(transaction);
    }
}
