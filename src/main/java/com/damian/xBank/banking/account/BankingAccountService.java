package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountUpdateRequest;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.exception.CustomerException;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BankingAccountService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BankingAccountRepository bankingAccountRepository;
    private final CustomerRepository customerRepository;

    private final Faker faker;

    public BankingAccountService(BCryptPasswordEncoder bCryptPasswordEncoder, BankingAccountRepository bankingAccountRepository, CustomerRepository customerRepository, Faker faker) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.bankingAccountRepository = bankingAccountRepository;
        this.customerRepository = customerRepository;
        this.faker = faker;
    }

    // return all the BankingsAccount that belongs to customerId
    public Set<BankingAccountDTO> getBankingAccounts(Long customerId) {
        return bankingAccountRepository.findById(customerId).stream().map(
                BankingAccount::toDTO
        ).collect(Collectors.toSet());
    }

    public BankingAccount updateBankingAccount(BankingAccountUpdateRequest request) {
        return null;
    }

    public BankingAccount openBankingAccount(BankingAccountOpenRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // we get the Customer entity so we can save at the end
        final Customer customer = customerRepository.findByEmail(customerLogged.getEmail()).orElseThrow(
                () -> new CustomerException("Customer cannot be found")
        );

        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setCustomer(customer);
        bankingAccount.setAccountType(request.accountType());
        bankingAccount.setAccountCurrency(request.accountCurrency());
        bankingAccount.setAccountNumber(this.generateAccountNumber());

        return bankingAccountRepository.save(bankingAccount);
    }

    public BankingAccount closeBankingAccount(Long id) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // we get the Customer entity so we can save at the end
        final Customer customer = customerRepository.findByEmail(customerLogged.getEmail()).orElseThrow(
                () -> new CustomerException("Customer cannot be found")
        );

        final BankingAccount bankingAccount = bankingAccountRepository.findById(id).orElseThrow(
                () -> new BankingAccountException("BankingAccount cannot be found")
        );

        bankingAccount.setAccountStatus(BankingAccountStatus.CLOSED);

        return bankingAccountRepository.save(bankingAccount);
    }

    public String generateAccountNumber() {
        return faker.finance().iban();
    }
}
