package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardMaximumCardsPerAccountLimitReached;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.card.http.BankingCardCreateRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Service
public class BankingCardService {
    private final int MAX_CARDS_PER_ACCOUNT = 5;
    private final BankingCardRepository bankingCardRepository;
    private final BankingAccountRepository bankingAccountRepository;
    private final CustomerRepository customerRepository;
    private final Faker faker;
    private final BankingAccountService bankingAccountService;

    public BankingCardService(
            BankingCardRepository bankingCardRepository,
            BankingAccountRepository bankingAccountRepository,
            CustomerRepository customerRepository,
            Faker faker, BankingAccountService bankingAccountService
    ) {
        this.bankingCardRepository = bankingCardRepository;
        this.bankingAccountRepository = bankingAccountRepository;
        this.customerRepository = customerRepository;
        this.faker = faker;
        this.bankingAccountService = bankingAccountService;
    }

    public Set<BankingCard> getBankingCards() {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return this.getBankingCards(customerLogged.getId());
    }

    public Set<BankingCard> getBankingCards(Long customerId) {
        return bankingCardRepository.findCardsByCustomerId(customerId);
    }

    public BankingTransaction spend(
            Long bankingCardId,
            BankingAccountTransactionCreateRequest request
    ) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // bankingCard to be used
        BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                () -> new BankingCardNotFoundException(bankingCardId)
        );

        // if the owner of the card is not the current logged customer.
        if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
            throw new BankingCardAuthorizationException();
        }

        final boolean isCardDisabled = bankingCard.getCardStatus().equals(BankingCardStatus.DISABLED);
        final boolean isCardLocked = bankingCard.getLockStatus().equals(BankingCardLockStatus.LOCKED);

        if (isCardDisabled) {
            throw new BankingCardAuthorizationException("The card is disabled.");
        }

        if (isCardLocked) {
            throw new BankingCardAuthorizationException("The card is locked.");
        }

        // we return the transaction
        return bankingAccountService.handleCreateTransactionRequest(
                bankingCard.getAssociatedBankingAccount().getId(), request
        );
    }

    public BankingCard createCard(Long bankingAccountId, BankingCardCreateRequest request) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // we get the BankingAccount where the card will be created.
        final BankingAccount bankingAccount = bankingAccountRepository
                .findById(bankingAccountId)
                .orElseThrow(
                        () -> new BankingAccountNotFoundException(bankingAccountId)
                );

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                throw new BankingAccountAuthorizationException(bankingAccountId);
            }
        }

        // count how many active (ENABLED) cards has this account and check if exceeds the limit.
        if (countActiveCards(bankingAccount) >= MAX_CARDS_PER_ACCOUNT) {
            throw new BankingCardMaximumCardsPerAccountLimitReached();
        }

        // create the card and associate to the account
        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardCvv(this.generateCardCVV());
        bankingCard.setCardPin(this.generateCardPIN());
        bankingCard.setExpiredDate(LocalDate.now().plusMonths(24));
        bankingCard.setAssociatedBankingAccount(bankingAccount);
        bankingCard.setCardType(request.cardType());
        bankingCard.setCardNumber(this.generateCardNumber());

        // save the card
        return bankingCardRepository.save(bankingCard);
    }

    public BankingCard lockCard(Long bankingCardId) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Banking account to be closed
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking account not found
                () -> new BankingAccountNotFoundException(bankingCardId));

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingCardAuthorizationException();
            }
        }

        // we mark the card as locked
        bankingCard.setLockStatus(BankingCardLockStatus.LOCKED);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(bankingCard);
    }

    public BankingCard unlockCard(Long bankingCardId) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Banking account to be closed
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking account not found
                () -> new BankingAccountNotFoundException(bankingCardId));

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingCardAuthorizationException();
            }
        }

        // we mark the card as locked
        bankingCard.setLockStatus(BankingCardLockStatus.UNLOCKED);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(bankingCard);
    }

    public BankingCard cancelCard(Long bankingCardId) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Banking account to be closed
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking account not found
                () -> new BankingAccountNotFoundException(bankingCardId));

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingCardAuthorizationException();
            }
        }

        // we mark the account as closed
        bankingCard.setCardStatus(BankingCardStatus.DISABLED);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(bankingCard);
    }

    public BankingCard setBankingCardPin(Long bankingCardId, BankingCardSetPinRequest request) {
        // Customer logged
        final Customer actor = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Banking account to set pin on
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking account not found
                () -> new BankingAccountNotFoundException(bankingCardId));

        // if the logged customer is not admin
        if (!actor.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(actor.getId())) {
                // banking account does not belong to this customer
                throw new BankingCardAuthorizationException();
            }
        }

        // we set the new pin
        bankingCard.setCardPin(request.pin());

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(bankingCard);
    }

    private int countActiveCards(BankingAccount bankingAccount) {
        return (int) bankingAccount.getBankingCards().stream()
                                   .filter(bankingCard -> bankingCard.getCardStatus().equals(BankingCardStatus.ENABLED))
                                   .count();
    }

    public String generateCardNumber() {
        return faker.finance().creditCard();
    }

    public String generateCardCVV() {
        return faker.number().digits(3);
    }

    public String generateCardPIN() {
        return faker.number().digits(4);
    }
}
