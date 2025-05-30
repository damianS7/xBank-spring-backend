package com.damian.xBank.banking.card;

import com.damian.xBank.auth.http.PasswordConfirmationRequest;
import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionRepository;
import com.damian.xBank.banking.transactions.BankingTransactionService;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.common.utils.AuthCustomer;
import com.damian.xBank.customer.Customer;
import net.datafaker.Faker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Service
public class BankingCardService {

    private final BankingCardRepository bankingCardRepository;
    private final BankingTransactionRepository bankingTransactionRepository;
    private final Faker faker;
    private final BankingTransactionService bankingTransactionService;

    public BankingCardService(
            BankingCardRepository bankingCardRepository,
            BankingTransactionRepository bankingTransactionRepository,
            Faker faker,
            BankingTransactionService bankingTransactionService
    ) {
        this.bankingCardRepository = bankingCardRepository;
        this.bankingTransactionRepository = bankingTransactionRepository;
        this.faker = faker;
        this.bankingTransactionService = bankingTransactionService;
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
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

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
        return bankingTransactionService.handleCreateTransactionRequest(
                bankingCard.getAssociatedBankingAccount().getId(), request
        );
    }

    public Page<BankingTransaction> getBankingCardTransactions(Long bankingCardId, Pageable pageable) {
        return bankingTransactionRepository.findByBankingCardId(bankingCardId, pageable);
    }

    public BankingCard createCard(BankingAccount bankingAccount, BankingCardType cardType) {
        // create the card and associate to the account
        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardCvv(this.generateCardCVV());
        bankingCard.setCardPin(this.generateCardPIN());
        bankingCard.setCardNumber(this.generateCardNumber());
        bankingCard.setExpiredDate(LocalDate.now().plusMonths(24));
        bankingCard.setCardType(cardType);
        bankingCard.setCreatedAt(Instant.now());
        bankingCard.setUpdatedAt(Instant.now());
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        // save the card
        return bankingCardRepository.save(bankingCard);
    }

    public BankingCard lockCardRequest(
            Long bankingCardId,
            PasswordConfirmationRequest request
    ) {
        return this.setCardLockStatusRequest(bankingCardId, BankingCardLockStatus.LOCKED, request);
    }

    public BankingCard unlockCardRequest(
            Long bankingCardId,
            PasswordConfirmationRequest request
    ) {
        return this.setCardLockStatusRequest(bankingCardId, BankingCardLockStatus.UNLOCKED, request);
    }

    public BankingCard setCardLockStatusRequest(
            Long bankingCardId,
            BankingCardLockStatus cardLockStatus,
            PasswordConfirmationRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking account to be closed
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking account not found
                () -> new BankingCardNotFoundException(bankingCardId));

        // if the logged customer is admin just set the lock status and skip checks
        if (!AuthCustomer.isAdmin(customerLogged)) {
            // check if the card to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
                // banking card does not belong to this customer
                throw new BankingCardAuthorizationException();
            }

            // password validation
            if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
                throw new PasswordMismatchException("Password does not match.");
            }
        }

        return this.setCardLockStatus(bankingCard, cardLockStatus);
    }

    public BankingCard setCardLockStatus(BankingCard card, BankingCardLockStatus cardLockStatus) {
        // we mark the card as locked
        card.setLockStatus(cardLockStatus);

        // we change the updateAt timestamp field
        card.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(card);
    }

    public BankingCard setDailyLimitRequest(Long bankingCardId, BankingCardSetDailyLimitRequest request) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking card to be closed
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking card not found
                () -> new BankingCardNotFoundException(bankingCardId));

        // if the logged customer is not admin
        if (!AuthCustomer.isAdmin(customerLogged)) {
            // check if the card to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
                // banking card does not belong to this customer
                throw new BankingCardAuthorizationException();
            }

            // password validation
            if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
                throw new PasswordMismatchException("Password does not match.");
            }
        }

        return this.setDailyLimit(bankingCard, request.dailyLimit());
    }

    public BankingCard setDailyLimit(BankingCard bankingCard, BigDecimal dailyLimit) {
        // we set the limit of the card
        bankingCard.setDailyLimit(dailyLimit);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingCard
        return bankingCardRepository.save(bankingCard);
    }

    public BankingCard cancelCardRequest(Long bankingCardId, PasswordConfirmationRequest request) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking card to be cancel
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking account not found
                () -> new BankingCardNotFoundException(bankingCardId));

        // if the logged customer is not admin
        if (!AuthCustomer.isAdmin(customerLogged)) {
            // check if the card to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
                // banking card does not belong to this customer
                throw new BankingCardAuthorizationException();
            }

            // password validation
            if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
                throw new PasswordMismatchException("Password does not match.");
            }
        }

        return this.cancelCard(bankingCard);
    }

    public BankingCard cancelCard(BankingCard bankingCard) {
        // we mark the card as disabled
        bankingCard.setCardStatus(BankingCardStatus.DISABLED);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingCard
        return bankingCardRepository.save(bankingCard);
    }

    public BankingCard setBankingCardPinRequest(Long bankingCardId, BankingCardSetPinRequest request) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking card to set pin on
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking card not found
                () -> new BankingAccountNotFoundException(bankingCardId));

        // if the logged customer is not admin
        if (!AuthCustomer.isAdmin(customerLogged)) {
            // check if the card to be closed belongs to this customer.
            if (!bankingCard.getCardOwner().getId().equals(customerLogged.getId())) {
                // banking card does not belong to this customer
                throw new BankingCardAuthorizationException();
            }

            // password validation
            if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
                System.out.println(request.password());
                System.out.println(customerLogged.getPassword());
                throw new PasswordMismatchException("Password does not match.");
            }
        }

        return this.setBankingCardPin(bankingCard, request.pin());
    }

    public BankingCard setBankingCardPin(BankingCard bankingCard, String pin) {
        // we set the new pin
        bankingCard.setCardPin(pin);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(bankingCard);
    }

    public String generateCardNumber() {
        return faker.number().digits(16);
    }

    public String generateCardCVV() {
        return faker.number().digits(3);
    }

    public String generateCardPIN() {
        return faker.number().digits(4);
    }
}
