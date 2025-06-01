package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardService;
import com.damian.xBank.banking.card.BankingCardStatus;
import com.damian.xBank.banking.card.exception.BankingCardMaximumCardsPerAccountLimitReached;
import com.damian.xBank.banking.card.http.BankingCardRequest;
import com.damian.xBank.common.utils.AuthCustomer;
import com.damian.xBank.customer.Customer;
import org.springframework.stereotype.Service;

@Service
public class BankingAccountCardManagerService {
    private final int MAX_CARDS_PER_ACCOUNT = 5;
    private final BankingCardService bankingCardService;
    private final BankingAccountRepository bankingAccountRepository;

    public BankingAccountCardManagerService(
            BankingAccountRepository bankingAccountRepository,
            BankingCardService bankingCardService
    ) {
        this.bankingAccountRepository = bankingAccountRepository;
        this.bankingCardService = bankingCardService;
    }

    public BankingCard requestBankingCard(Long bankingAccountId, BankingCardRequest request) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // we get the BankingAccount to associate the card created.
        final BankingAccount bankingAccount = bankingAccountRepository
                .findById(bankingAccountId)
                .orElseThrow(
                        () -> new BankingAccountNotFoundException(
                                BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                        )
                );

        // if the logged customer is not admin
        if (!AuthCustomer.isAdmin(customerLogged)) {
            // check if the account belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                throw new BankingAccountAuthorizationException(
                        BankingAccountAuthorizationException.ACCOUNT_NOT_BELONG_TO_CUSTOMER
                );
            }

            // check password
        }

        // if customer has reached the maximum amount of cards per account.
        if (countActiveCards(bankingAccount) >= MAX_CARDS_PER_ACCOUNT) {
            throw new BankingCardMaximumCardsPerAccountLimitReached(
                    BankingCardMaximumCardsPerAccountLimitReached.LIMIT_REACHED
            );
        }

        // create the card and associate to the account and return it.
        return bankingCardService.createBankingCard(bankingAccount, request.cardType());
    }

    // It counts how many active (ENABLED) cards has this account
    private int countActiveCards(BankingAccount bankingAccount) {
        return (int) bankingAccount
                .getBankingCards()
                .stream()
                .filter(bankingCard -> bankingCard.getCardStatus().equals(BankingCardStatus.ENABLED))
                .count();
    }
}
