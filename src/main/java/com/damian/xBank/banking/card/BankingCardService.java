package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BankingCardService {
    private final BankingCardRepository bankingCardRepository;
    private final BankingAccountRepository bankingAccountRepository;
    private final CustomerRepository customerRepository;
    private final Faker faker;

    public BankingCardService(
            BankingCardRepository bankingCardRepository,
            BankingAccountRepository bankingAccountRepository,
            CustomerRepository customerRepository,
            Faker faker
    ) {
        this.bankingCardRepository = bankingCardRepository;
        this.bankingAccountRepository = bankingAccountRepository;
        this.customerRepository = customerRepository;
        this.faker = faker;
    }

    public BankingCard requestCard(Long bankingAccountId, BankingCardOpenRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
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
            if (!bankingAccount.getCustomer().getId().equals(customerLogged.getId())) {
                throw new BankingAccountAuthorizationException(bankingAccountId);
            }
        }

        // create the card and associate to the account
        BankingCard bankingCard = new BankingCard();
        bankingCard.setLinkedBankingAccount(bankingAccount);
        bankingCard.setCardType(request.cardType());
        bankingCard.setCardNumber(this.generateCardNumber());

        // save the card
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

        // save the data and return BankingAccount
        return bankingCardRepository.save(bankingCard);
    }

    public String generateCardNumber() {
        return faker.finance().creditCard();
    }
}
