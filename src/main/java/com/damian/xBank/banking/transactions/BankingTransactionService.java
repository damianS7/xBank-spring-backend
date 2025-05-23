package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.transactions.exception.BankingTransactionAuthorizationException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionNotFoundException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BankingTransactionService {
    private final BankingAccountRepository bankingAccountRepository;
    private final BankingTransactionRepository bankingTransactionRepository;
    private final CustomerRepository customerRepository;
    private final Faker faker;

    public BankingTransactionService(
            BankingAccountRepository bankingAccountRepository,
            BankingTransactionRepository bankingTransactionRepository,
            CustomerRepository customerRepository,
            Faker faker
    ) {
        this.bankingAccountRepository = bankingAccountRepository;
        this.bankingTransactionRepository = bankingTransactionRepository;
        this.customerRepository = customerRepository;
        this.faker = faker;
    }

    public BankingTransaction patchStatusTransaction(
            Long bankingTransactionId,
            BankingTransactionPatchRequest request
    ) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // banking account does not belong to this customer
            throw new BankingTransactionAuthorizationException();
        }

        // Banking account to be closed
        final BankingTransaction bankingTransaction = bankingTransactionRepository
                .findById(bankingTransactionId)
                .orElseThrow(
                        () -> new BankingTransactionNotFoundException(
                                bankingTransactionId)
                );


        // we mark the account as closed
        bankingTransaction.setTransactionStatus(request.transactionStatus());

        // we change the updateAt timestamp field
        bankingTransaction.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingTransactionRepository.save(bankingTransaction);
    }
}
