package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardLockStatus;
import com.damian.xBank.banking.card.BankingCardRepository;
import com.damian.xBank.banking.card.BankingCardStatus;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.card.http.BankingCardSpendRequest;
import com.damian.xBank.banking.card.http.BankingCardWithdrawalRequest;
import com.damian.xBank.common.utils.AuthCustomer;
import com.damian.xBank.customer.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankingCardTransactionService {

    private final BankingCardRepository bankingCardRepository;
    private final BankingTransactionService bankingTransactionService;

    public BankingCardTransactionService(
            BankingCardRepository bankingCardRepository,
            BankingTransactionService bankingTransactionService
    ) {
        this.bankingCardRepository = bankingCardRepository;
        this.bankingTransactionService = bankingTransactionService;
    }

    public BankingTransaction spendRequest(
            Long bankingCardId,
            BankingCardSpendRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // bankingCard to be used
        BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                () -> new BankingCardNotFoundException(bankingCardId)
        );

        // if the owner of the card is not the current logged customer.
        if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
            throw new BankingCardAuthorizationException();
        }

        if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
            throw new BankingCardAuthorizationException();
        }

        return this.spend(bankingCard, request.amount(), request.description());
    }

    public BankingTransaction spend(
            BankingCard card,
            BigDecimal amount,
            String description
    ) {
        final boolean isCardDisabled = card.getCardStatus().equals(BankingCardStatus.DISABLED);
        final boolean isCardLocked = card.getLockStatus().equals(BankingCardLockStatus.LOCKED);

        if (isCardDisabled) {
            throw new BankingCardAuthorizationException("The card is disabled.");
        }

        if (isCardLocked) {
            throw new BankingCardAuthorizationException("The card is locked.");
        }

        BankingTransaction transaction = this.bankingTransactionService.generateTransaction(
                card,
                BankingTransactionType.CARD_CHARGE,
                amount,
                description
        );

        // check balance
        if (!card.hasEnoughFundsToSpend(amount)) {
            throw new BankingCardAuthorizationException("Insufficient funds.");
        }

        // if the transaction is created, deduce the amount from balance
        card.chargeAmount(amount);

        // transaction is completed
        transaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);

        // save the transaction
        return bankingTransactionService.storeTransaction(transaction);
    }

    public BankingTransaction withdrawalRequest(
            Long bankingCardId,
            BankingCardWithdrawalRequest request
    ) {
        return null;
    }

}
