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

    // security checks before card operations
    public void cardSecurity(
            BankingCard bankingCard,
            String inputPin
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // if the owner of the card is not the current logged customer.
        if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.CARD_DOES_NOT_BELONG_TO_CUSTOMER
            );
        }

        // check card pin
        if (!bankingCard.getCardPin().equals(inputPin)) {
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

    // validates card status and does the transaction
    public BankingTransaction spend(
            BankingCard card,
            BigDecimal amount,
            String description
    ) {
        BankingTransaction transaction = this.bankingTransactionService.createTransaction(
                card,
                BankingTransactionType.CARD_CHARGE,
                amount,
                description
        );

        // check balance
        if (!card.hasEnoughFundsToSpend(amount)) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.INSUFFICIENT_FUNDS
            );
        }

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
            BigDecimal amount
    ) {
        BankingTransaction transaction = this.bankingTransactionService.createTransaction(
                card,
                BankingTransactionType.WITHDRAWAL,
                amount,
                "ATM withdrawal."
        );

        // check balance
        if (!card.hasEnoughFundsToSpend(amount)) {
            throw new BankingCardAuthorizationException(
                    BankingCardAuthorizationException.INSUFFICIENT_FUNDS
            );
        }

        // if the transaction is created, deduce the amount from balance
        card.chargeAmount(amount);

        // transaction is completed
        transaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);

        // save the transaction
        return bankingTransactionService.persistTransaction(transaction);
    }

    // handle request and determine what to do.
    public BankingTransaction processCardTransaction(
            Long cardId,
            BankingCardTransactionRequest request
    ) {
        // bankingCard to be used
        BankingCard bankingCard = bankingCardRepository.findById(cardId).orElseThrow(
                () -> new BankingCardNotFoundException(
                        BankingCardNotFoundException.CARD_NOT_FOUND
                )
        );

        // run security checks before use the card
        cardSecurity(bankingCard, request.cardPin());

        return switch (request.transactionType()) {
            case CARD_CHARGE -> this.spend(bankingCard, request.amount(), request.description());
            case WITHDRAWAL -> this.withdrawal(bankingCard, request.amount());
            default -> throw new BankingTransactionException(
                    BankingTransactionException.INVALID_TRANSACTION_TYPE
            );
        };
    }
}
