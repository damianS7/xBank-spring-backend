package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardLockStatus;
import com.damian.xBank.banking.card.BankingCardRepository;
import com.damian.xBank.banking.card.BankingCardStatus;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionException;
import com.damian.xBank.banking.transactions.http.BankingCardTransactionRequest;
import com.damian.xBank.common.utils.AuthCustomer;
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
                        BankingCardNotFoundException.CARD_NOT_FOUND
                )
        );

        return switch (request.transactionType()) {
            case CARD_CHARGE -> this.spend(bankingCard, request.cardPin(), request.amount(), request.description());
            case WITHDRAWAL -> this.withdrawal(bankingCard, request.cardPin(), request.amount());
            default -> throw new BankingTransactionException(
                    BankingTransactionException.INVALID_TRANSACTION_TYPE
            );
        };
    }

    // security checks before card operations
    public void validateCustomerAuthorization(
            BankingCard bankingCard
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // if the owner of the card is not the current logged customer.
        if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.CARD_DOES_NOT_BELONG_TO_CUSTOMER
            );
        }
    }

    // security checks before card operations
    public void validateCardAuthorization(
            BankingCard bankingCard,
            String cardPIN
    ) {
        // check card pin
        if (!bankingCard.getCardPin().equals(cardPIN)) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.INVALID_PIN
            );
        }

        // check card status
        final boolean isCardDisabled = bankingCard.getCardStatus().equals(BankingCardStatus.DISABLED);
        final boolean isCardLocked = bankingCard.getLockStatus().equals(BankingCardLockStatus.LOCKED);

        if (isCardDisabled) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.CARD_DISABLED
            );
        }

        if (isCardLocked) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.CARD_LOCKED
            );
        }
    }

    public void validateCardFunds(BankingCard card, BigDecimal amount) {
        if (!card.hasEnoughFundsToSpend(amount)) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.INSUFFICIENT_FUNDS
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

        // check customer authorization
        this.validateCustomerAuthorization(card);

        // check card authorization
        this.validateCardAuthorization(card, cardPIN);

        // check balance
        this.validateCardFunds(card, amount);

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

        // check customer authorization
        this.validateCustomerAuthorization(card);

        // check card authorization
        this.validateCardAuthorization(card, cardPIN);

        // check balance
        this.validateCardFunds(card, amount);

        // if the transaction is created, deduce the amount from balance
        card.chargeAmount(amount);

        // transaction is completed
        transaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);

        // save the transaction
        return bankingTransactionService.persistTransaction(transaction);
    }
}
