package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.account.http.request.BankingAccountAliasUpdateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCloseRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCreateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.common.utils.AuthCustomer;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

// TODO rename methods AsAdmin ...
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

    // return all the BankingAccounts that belongs to the logged customer.
    public Set<BankingAccount> getLoggedCustomerBankingAccounts() {
        // we extract the customer logged from the SecurityContext
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

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
    public BankingAccount createBankingAccountForCustomer(Long customerId, BankingAccountCreateRequest request) {
        // we get the Customer entity so we can save at the end
        final Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(
                        CustomerNotFoundException.NOT_FOUND
                )
        );

        return this.createBankingAccount(customer, request.accountType(), request.accountCurrency());
    }

    // create a BankingAccount for the logged customer
    public BankingAccount createBankingAccountForLoggedCustomer(BankingAccountCreateRequest request) {
        // we extract the customer logged from the SecurityContext
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        return this.createBankingAccountForCustomer(customerLogged.getId(), request);
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
                        BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                ) // Banking account not found
        );

        return this.updateBankingAccountStatus(bankingAccount, BankingAccountStatus.OPEN);
    }

    // Logged customer open a BankingAccount
    public BankingAccount openBankingAccountForLoggedCustomer(
            Long bankingAccountId,
            BankingAccountOpenRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking account to be open
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                ) // Banking account not found
        );

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingAccountAuthorizationException(
                        BankingAccountAuthorizationException.ACCOUNT_NOT_BELONG_TO_CUSTOMER
                );
            }

            // check password
            if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
                throw new BankingAccountAuthorizationException("Wrong password");
            }
        }

        // suspended accounts can only change status by admin
        if (bankingAccount.getAccountStatus().equals(BankingAccountStatus.SUSPENDED)) {
            throw new BankingAccountAuthorizationException("Only admin can open a suspended account");
        }

        return this.updateBankingAccountStatus(bankingAccount, BankingAccountStatus.OPEN);
    }

    // (admin) close a BankingAccount
    public BankingAccount closeBankingAccount(Long bankingAccountId) {
        // Banking account to to close
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                ) // Banking account not found
        );

        return this.updateBankingAccountStatus(bankingAccount, BankingAccountStatus.CLOSED);
    }

    // Logged customer close a BankingAccount
    public BankingAccount closeBankingAccountForLoggedCustomer(
            Long bankingAccountId,
            BankingAccountCloseRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking account to be closed
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                ) // Banking account not found
        );

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingAccountAuthorizationException(
                        BankingAccountAuthorizationException.ACCOUNT_NOT_BELONG_TO_CUSTOMER
                );
            }

            // check password
            if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
                throw new BankingAccountAuthorizationException("Wrong password");
            }
        }

        // suspended accounts can only change status by admin
        if (bankingAccount.getAccountStatus().equals(BankingAccountStatus.SUSPENDED)) {
            throw new BankingAccountAuthorizationException("Only admin can close a suspended account");
        }

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
            BankingAccountAliasUpdateRequest request
    ) {

        // Banking account to set an alias
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                ) // Banking account not found
        );

        return this.setBankingAccountAlias(bankingAccount, request.alias());
    }

    // Logged customer set an alias for an account
    public BankingAccount setBankingAccountAliasForLoggedCustomer(
            Long bankingAccountId,
            BankingAccountAliasUpdateRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthCustomer.getLoggedCustomer();

        // Banking account to set alias
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                ) // Banking account not found
        );

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingAccountAuthorizationException("You are not the owner of this account.");
            }

            // check password
            if (!AuthCustomer.isPasswordCorrect(request.password(), customerLogged.getPassword())) {
                throw new BankingAccountAuthorizationException("Wrong password");
            }
        }

        return this.setBankingAccountAlias(bankingAccount, request.alias());
    }

    public String generateAccountNumber() {
        //ES00 0000 0000 0000 0000 0000
        String country = faker.country().countryCode2().toUpperCase();
        return country + faker.number().digits(22);
    }
}