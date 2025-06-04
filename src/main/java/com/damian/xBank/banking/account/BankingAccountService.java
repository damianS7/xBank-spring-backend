package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.account.http.request.BankingAccountAliasUpdateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCloseRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCreateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.common.utils.AuthUtils;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class BankingAccountService {
    private final CustomerRepository customerRepository;
    private final BankingAccountRepository bankingAccountRepository;
    private final Faker faker;

    public BankingAccountService(
            BankingAccountRepository bankingAccountRepository,
            CustomerRepository customerRepository,
            Faker faker
    ) {
        this.bankingAccountRepository = bankingAccountRepository;
        this.customerRepository = customerRepository;
        this.faker = faker;
    }

    private void validateAuthorizationOrThrow(BankingAccount account, Customer customer, String password) {
        this.checkCustomerAuthorization(customer, account, password);
    }

    private void checkCustomerAuthorization(Customer customer, BankingAccount account, String password) {
        // if the logged customer is not admin
        if (!customer.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            this.checkAccountOwnership(account, customer);

            // check password
            AuthUtils.validatePasswordOrElseThrow(password, customer);
        }
    }

    // check ownership of a BankingAccount
    private void checkAccountOwnership(BankingAccount bankingAccount, Customer customer) {
        // check if the account to be closed belongs to this customer.
        if (!bankingAccount.getOwner().getId().equals(customer.getId())) {
            // banking account does not belong to this customer
            throw new BankingAccountAuthorizationException(
                    Exceptions.ACCOUNT.ACCESS_FORBIDDEN
            );
        }
    }

    // check account is not suspended
    private void checkAccountNotSuspendedOrThrow(BankingAccount bankingAccount) {
        // suspended accounts can only change status by admin
        if (bankingAccount.getAccountStatus().equals(BankingAccountStatus.SUSPENDED)) {
            throw new BankingAccountAuthorizationException(Exceptions.ACCOUNT.SUSPENDED);
        }
    }

    // return all the BankingAccounts that belongs to the logged customer.
    public Set<BankingAccount> getLoggedCustomerBankingAccounts() {
        // we extract the customer logged from the SecurityContext
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        return this.getCustomerBankingAccounts(customerLogged.getId());
    }

    // return all the BankingAccounts that belongs to customerId.
    public Set<BankingAccount> getCustomerBankingAccounts(Long customerId) {
        return bankingAccountRepository.findByCustomer_Id(customerId);
    }

    private BankingAccount createBankingAccount(
            Customer customerOwner,
            BankingAccountType accountType,
            BankingAccountCurrency accountCurrency
    ) {
        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setAccountStatus(BankingAccountStatus.CLOSED);
        bankingAccount.setOwner(customerOwner);
        bankingAccount.setAccountType(accountType);
        bankingAccount.setAccountCurrency(accountCurrency);
        bankingAccount.setAccountNumber(this.generateAccountNumber());
        return bankingAccountRepository.save(bankingAccount);
    }

    // (admin) create a BankingAccount for a specific customer
    public BankingAccount createBankingAccount(Long customerId, BankingAccountCreateRequest request) {
        // we get the Customer entity so we can save at the end
        final Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(
                        Exceptions.CUSTOMER.NOT_FOUND
                )
        );

        return this.createBankingAccount(customer, request.accountType(), request.accountCurrency());
    }

    // create a BankingAccount for the logged customer
    public BankingAccount createBankingAccount(BankingAccountCreateRequest request) {
        // we extract the customer logged from the SecurityContext
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        return this.createBankingAccount(customerLogged.getId(), request);
    }

    private BankingAccount updateBankingAccountStatus(
            BankingAccount bankingAccount,
            BankingAccountStatus accountStatus
    ) {
        // we mark the account as closed
        bankingAccount.setAccountStatus(accountStatus);

        // we change the updateAt timestamp field
        bankingAccount.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingAccountRepository.save(bankingAccount);
    }

    // (admin) open a BankingAccount
    public BankingAccount openBankingAccount(Long bankingAccountId) {
        // Banking account to to open
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        Exceptions.ACCOUNT.NOT_FOUND
                ) // Banking account not found
        );

        return this.updateBankingAccountStatus(bankingAccount, BankingAccountStatus.OPEN);
    }

    // Logged customer open a BankingAccount
    public BankingAccount openBankingAccount(
            Long bankingAccountId,
            BankingAccountOpenRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // Banking account to be open
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        Exceptions.ACCOUNT.NOT_FOUND
                ) // Banking account not found
        );

        // validate authorization
        this.validateAuthorizationOrThrow(bankingAccount, customerLogged, request.password());

        // suspended accounts can only change status by admin
        this.checkAccountNotSuspendedOrThrow(bankingAccount);

        return this.updateBankingAccountStatus(bankingAccount, BankingAccountStatus.OPEN);
    }

    // (admin) close a BankingAccount
    public BankingAccount closeBankingAccount(Long bankingAccountId) {
        // Banking account to to close
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        Exceptions.ACCOUNT.NOT_FOUND
                ) // Banking account not found
        );

        return this.updateBankingAccountStatus(bankingAccount, BankingAccountStatus.CLOSED);
    }

    // Logged customer close a BankingAccount
    public BankingAccount closeBankingAccount(
            Long bankingAccountId,
            BankingAccountCloseRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // Banking account to be closed
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        Exceptions.ACCOUNT.NOT_FOUND
                ) // Banking account not found
        );

        // validate authorization
        this.validateAuthorizationOrThrow(bankingAccount, customerLogged, request.password());

        // suspended accounts can only change status by admin
        this.checkAccountNotSuspendedOrThrow(bankingAccount);

        return this.updateBankingAccountStatus(bankingAccount, BankingAccountStatus.CLOSED);
    }

    // set an alias for an account
    private BankingAccount setBankingAccountAlias(BankingAccount bankingAccount, String alias) {
        // we mark the account as closed
        bankingAccount.setAlias(alias);

        // we change the updateAt timestamp field
        bankingAccount.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingAccountRepository.save(bankingAccount);
    }

    // (admin) set an alias for an account
    public BankingAccount setBankingAccountAlias(
            Long bankingAccountId,
            String alias
    ) {

        // Banking account to set an alias
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        Exceptions.ACCOUNT.NOT_FOUND
                ) // Banking account not found
        );

        return this.setBankingAccountAlias(bankingAccount, alias);
    }

    // Logged customer set an alias for an account
    public BankingAccount setBankingAccountAlias(
            Long bankingAccountId,
            BankingAccountAliasUpdateRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // Banking account to set alias
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        Exceptions.ACCOUNT.NOT_FOUND
                ) // Banking account not found
        );

        // validate authorization
        this.validateAuthorizationOrThrow(bankingAccount, customerLogged, request.password());

        return this.setBankingAccountAlias(bankingAccount, request.alias());
    }

    public String generateAccountNumber() {
        //ES00 0000 0000 0000 0000 0000
        String country = faker.country().countryCode2().toUpperCase();
        return country + faker.number().digits(22);
    }
}