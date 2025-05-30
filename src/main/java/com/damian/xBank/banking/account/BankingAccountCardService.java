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
public class BankingAccountCardService {
    private final int MAX_CARDS_PER_ACCOUNT = 5;
    private final BankingCardService bankingCardService;
    private final BankingAccountRepository bankingAccountRepository;

    public BankingAccountCardService(
            BankingAccountRepository bankingAccountRepository,
            BankingCardService bankingCardService
    ) {
        this.bankingAccountRepository = bankingAccountRepository;
        this.bankingCardService = bankingCardService;
    }


    public BankingCard requestBankingCard(Long bankingAccountId, BankingCardRequest request) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // we get the BankingAccount where the card will be created.
        final BankingAccount bankingAccount = bankingAccountRepository
                .findById(bankingAccountId)
                .orElseThrow(
                        () -> new BankingAccountNotFoundException(bankingAccountId)
                );

        // if the logged customer is not admin
        if (!AuthCustomer.isAdmin(customerLogged)) {
            // check if the account belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                throw new BankingAccountAuthorizationException(bankingAccountId);
            }

            // check password
        }

        // count how many active (ENABLED) cards has this account and check if exceeds the limit.
        if (countActiveCards(bankingAccount) >= MAX_CARDS_PER_ACCOUNT) {
            throw new BankingCardMaximumCardsPerAccountLimitReached();
        }

        // create the card and associate to the account
        return bankingCardService.createCard(bankingAccount, request.cardType());
    }

    private int countActiveCards(BankingAccount bankingAccount) {
        return (int) bankingAccount
                .getBankingCards()
                .stream()
                .filter(bankingCard -> bankingCard.getCardStatus().equals(BankingCardStatus.ENABLED))
                .count();
    }
}
