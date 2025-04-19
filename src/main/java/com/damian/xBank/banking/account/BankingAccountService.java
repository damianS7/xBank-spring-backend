package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountUpdateRequest;
import com.damian.xBank.banking.account.transactions.BankingAccountTransaction;
import com.damian.xBank.banking.account.transactions.BankingAccountTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.exception.CustomerException;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    // create a new transaction
    public BankingAccountTransaction createTransaction(
            Long bankingAccountId_from,
            BankingAccountTransactionCreateRequest request) {
        final boolean isTransfer = request.transactionType().equals(BankingAccountTransactionType.TRANSFER_TO);

        // if it is not a transter we just create one transaction
        if (!isTransfer) {
            return this.createTransaction(
                    bankingAccountId_from,
                    request.amount(),
                    request.transactionType(),
                    request.description()
            );
        }

        // this is a transter from bankingAccountId_source to bankingAccountId_to
        // we check the receiver id is defined in the request and its not null
        if (request.bankingAccountId_to() == null) {
            throw new BankingAccountException("Receiver id cannot be null");
        }

        // check that you are no sending to same acc
        if (bankingAccountId_from.equals(request.bankingAccountId_to())) {
            throw new BankingAccountException("You cannot transfer to the same account");
        }

        // we check if the banking account receiver exists
        BankingAccount toBankingAccount = bankingAccountRepository.findById(request.bankingAccountId_to())
                .orElseThrow(
                        () -> new BankingAccountException("Destination banking account not found")
                );

        // we create the senderTransaction for the sender
        BankingAccountTransaction senderTransaction = this.createTransaction(
                bankingAccountId_from,
                request.amount(),
                request.transactionType(),
                request.description()
        );

        // we create a secondary senderTransaction for the receiver.
        this.createTransaction(
                request.bankingAccountId_to(),
                request.amount(),
                BankingAccountTransactionType.TRANSFER_FROM,
                "Transfer from " + toBankingAccount.getCustomer().getFullName().toUpperCase()
        );

        // we return the senderTransaction that would be returned to the customer
        return senderTransaction;
    }

    @Transactional
    public BankingAccountTransaction createTransaction(
            Long bankingAccountId,
            BigDecimal amount,
            BankingAccountTransactionType transactionType,
            String description
    ) {
        BankingAccount customerBankingAccount = bankingAccountRepository.findById(bankingAccountId).orElseThrow(
                () -> new BankingAccountException("Banking Account not found")
        );

        // if the transaction is to spend from the account
        if (transactionType.equals(BankingAccountTransactionType.WITHDRAWAL)
                || transactionType.equals(BankingAccountTransactionType.CARD_CHARGE)
                || transactionType.equals(BankingAccountTransactionType.TRANSFER_TO)) {

            // check if customer can afford the transaction
            if (!this.hasSufficientBalance(customerBankingAccount.getBalance(), amount)) {
                throw new BankingAccountException("Insufficient funds");
            }

            // deduce the amount from the balance
            customerBankingAccount.setBalance(
                    customerBankingAccount.getBalance().subtract(amount)
            );
        }

        // if the transaction is receive to customer account
        if (transactionType.equals(BankingAccountTransactionType.DEPOSIT)
                || transactionType.equals(BankingAccountTransactionType.TRANSFER_FROM)) {

            // add the amount to the balance
            customerBankingAccount.setBalance(
                    customerBankingAccount.getBalance().add(amount)
            );
        }

        // we create the transaction in order to save it
        BankingAccountTransaction transaction = new BankingAccountTransaction(customerBankingAccount);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setDescription(description);

        // we add the transaction to the account
        customerBankingAccount.addAccountTransaction(transaction);

        // we save the transaction
        bankingAccountRepository.save(customerBankingAccount);

        // we return the created transaction
        return transaction;
    }

    // returns true if the operation can be carried
    public boolean hasSufficientBalance(BigDecimal balance, BigDecimal amountToSpend) {
        // if its 0 then balance is equal to the amount willing to spend
        // if its 1 then balance is greater than the amount willing to spend
        return balance.compareTo(amountToSpend) >= 0;
    }

    // return all the BankingsAccount that belongs to customerId
    public Set<BankingAccountDTO> getBankingAccounts(Long customerId) {
        return bankingAccountRepository.findByCustomer_Id(customerId).stream().map(
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
