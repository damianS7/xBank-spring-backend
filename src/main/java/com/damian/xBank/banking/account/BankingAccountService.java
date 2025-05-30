package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.account.http.request.*;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class BankingAccountService {
    private final CustomerRepository customerRepository;
    private final BankingAccountRepository bankingAccountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Faker faker;

    public BankingAccountService(
            BankingAccountRepository bankingAccountRepository,
            CustomerRepository customerRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            Faker faker
    ) {
        this.bankingAccountRepository = bankingAccountRepository;
        this.customerRepository = customerRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.faker = faker;
    }

    public BankingTransaction transferRequest(Long bankingAccountId, BankingAccountTransferRequest request) {
        return null;
    }

    public BankingTransaction transferTo() {
        return null;
    }

    public BankingTransaction deposit() {
        return null;
    }

    public BankingTransaction withdrawal() {
        return null;
    }

    // return all the BankingAccounts that belongs to the logged customer.
    public Set<BankingAccount> getLoggedCustomerBankingAccounts() {
        // we extract the customer logged from the SecurityContext
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return this.getCustomerBankingAccounts(customerLogged.getId());
    }

    // return all the BankingAccounts that belongs to the logged customer.
    public Set<BankingAccount> getCustomerLoggedBankingAccounts() {
        // we extract the customer logged from the SecurityContext
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return this.getCustomerBankingAccounts(customerLogged.getId());
    }

    // return all the BankingAccounts that belongs to customerId.
    public Set<BankingAccount> getCustomerBankingAccounts(Long customerId) {
        return bankingAccountRepository.findByCustomer_Id(customerId);
    }

    public BankingAccount createBankingAccount(BankingAccountCreateRequest request) {
        // we extract the customer logged from the SecurityContext
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // we get the Customer entity so we can save at the end
        final Customer customer = customerRepository.findByEmail(customerLogged.getEmail()).orElseThrow(
                () -> new CustomerNotFoundException(customerLogged.getEmail())
        );

        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setOwner(customer);
        bankingAccount.setAccountType(request.accountType());
        bankingAccount.setAccountCurrency(request.accountCurrency());
        bankingAccount.setAccountNumber(this.generateAccountNumber());

        return bankingAccountRepository.save(bankingAccount);
    }

    public BankingAccount openBankingAccount(Long bankingAccountId, BankingAccountOpenRequest request) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Banking account to be closed
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(bankingAccountId) // Banking account not found
        );

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingAccountAuthorizationException();
            }

            // TODO check password from request
        }

        // we mark the account as closed
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);

        // we change the updateAt timestamp field
        bankingAccount.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingAccountRepository.save(bankingAccount);
    }

    public BankingAccount closeBankingAccount(Long bankingAccountId, BankingAccountCloseRequest request) {
        // Customer logged
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Banking account to be closed
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(bankingAccountId) // Banking account not found
        );

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingAccountAuthorizationException();
            }

            // check password
            if (!bCryptPasswordEncoder.matches(request.password(), customerLogged.getPassword())) {
                throw new BankingAccountAuthorizationException("Wrong password");
            }
        }

        return this.closeBankingAccount(bankingAccount);
    }

    public BankingAccount closeBankingAccount(BankingAccount bankingAccount) {

        // we mark the account as closed
        bankingAccount.setAccountStatus(BankingAccountStatus.CLOSED);

        // we change the updateAt timestamp field
        bankingAccount.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingAccountRepository.save(bankingAccount);
    }

    public BankingAccount setBankingAccountAlias(
            Long bankingAccountId,
            BankingAccountAliasUpdateRequest request
    ) {
        return this.setBankingAccountAlias(bankingAccountId, request.alias());
    }

    public BankingAccount setBankingAccountAlias(Long bankingAccountId, String alias) {

        // Banking account to be closed
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(bankingAccountId) // Banking account not found
        );

        // we mark the account as closed
        bankingAccount.setAlias(alias);

        // save the data and return BankingAccount
        return bankingAccountRepository.save(bankingAccount);
    }

    private boolean isTransfer(BankingTransactionType transactionType) {
        return transactionType.equals(BankingTransactionType.TRANSFER_TO);
    }

    public String generateAccountNumber() {
        //ES00 0000 0000 0000 0000 0000
        String country = faker.country().countryCode2().toUpperCase();
        return country + faker.number().digits(22);
    }

}
