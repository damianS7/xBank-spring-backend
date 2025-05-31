package com.damian.xBank.banking.card;

import com.damian.xBank.auth.http.PasswordConfirmationRequest;
import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetLockStatusRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.common.utils.AuthCustomer;
import com.damian.xBank.customer.Customer;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Service
public class BankingCardService {

    private final BankingCardRepository bankingCardRepository;
    private final Faker faker;

    public BankingCardService(
            BankingCardRepository bankingCardRepository,
            Faker faker
    ) {
        this.bankingCardRepository = bankingCardRepository;
        this.faker = faker;
    }

    // return the cards of the logged customer
    public Set<BankingCard> getLoggedCustomerBankingCards() {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        return this.getCustomerBankingCards(customerLogged.getId());
    }

    // return the cards of a customer
    public Set<BankingCard> getCustomerBankingCards(Long customerId) {
        return bankingCardRepository.findCardsByCustomerId(customerId);
    }

    // create a new card and associate to the account
    public BankingCard createBankingCard(
            BankingAccount bankingAccount,
            BankingCardType cardType
    ) {
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

    // set the lock status of the card
    private BankingCard setCardLockStatus(
            BankingCard card,
            BankingCardLockStatus cardLockStatus
    ) {
        // we mark the card as locked
        card.setLockStatus(cardLockStatus);

        // we change the updateAt timestamp field
        card.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(card);
    }

    // (admin) set the lock status of the card.
    public BankingCard setCardLockStatusAsAdmin(
            Long bankingCardId,
            BankingCardLockStatus cardLockStatus
    ) {
        // Banking card to set lock status
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking card not found
                () -> new BankingCardNotFoundException(bankingCardId));

        return this.setCardLockStatus(bankingCard, cardLockStatus);
    }

    // set the lock status of the card for customers logged
    public BankingCard setCardLockStatus(
            Long bankingCardId,
            BankingCardSetLockStatusRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking account to be closed
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking card not found
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

        return this.setCardLockStatus(bankingCard, request.lockStatus());
    }

    // set the limit of the card
    private BankingCard setDailyLimit(
            BankingCard bankingCard,
            BigDecimal dailyLimit
    ) {
        // we set the limit of the card
        bankingCard.setDailyLimit(dailyLimit);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingCard
        return bankingCardRepository.save(bankingCard);
    }

    // (admin) set the limit of the card
    public BankingCard setDailyLimitAsAdmin(
            Long bankingCardId,
            BigDecimal dailyLimit
    ) {
        // Banking card to set limit
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking card not found
                () -> new BankingCardNotFoundException(bankingCardId));

        return this.setDailyLimit(bankingCard, dailyLimit);
    }

    // set the limit of the card for customers logged
    public BankingCard setDailyLimit(
            Long bankingCardId,
            BankingCardSetDailyLimitRequest request
    ) {
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

    // cancel the card
    private BankingCard cancelCard(BankingCard bankingCard) {
        // we mark the card as disabled
        bankingCard.setCardStatus(BankingCardStatus.DISABLED);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingCard
        return bankingCardRepository.save(bankingCard);
    }

    // (admin) cancel the card
    public BankingCard cancelCardAsAdmin(Long bankingCardId) {
        // Banking card to cancel
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking card not found
                () -> new BankingCardNotFoundException(bankingCardId));

        return this.cancelCard(bankingCard);
    }

    // cancel the card for customers logged
    public BankingCard cancelCard(
            Long bankingCardId,
            PasswordConfirmationRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking card to be cancel
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

        return this.cancelCard(bankingCard);
    }

    // set the pin
    private BankingCard setBankingCardPin(BankingCard bankingCard, String pin) {
        // we set the new pin
        bankingCard.setCardPin(pin);

        // we change the updateAt timestamp field
        bankingCard.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingCardRepository.save(bankingCard);
    }

    // (admin) set the pin
    public BankingCard setBankingCardPinAsAdmin(Long bankingCardId, String pin) {
        // Banking card to set pin
        final BankingCard bankingCard = bankingCardRepository.findById(bankingCardId).orElseThrow(
                // Banking card not found
                () -> new BankingCardNotFoundException(bankingCardId));

        return this.setBankingCardPin(bankingCard, pin);
    }

    // set the pin for customers logged
    public BankingCard setBankingCardPin(Long bankingCardId, BankingCardSetPinRequest request) {
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
