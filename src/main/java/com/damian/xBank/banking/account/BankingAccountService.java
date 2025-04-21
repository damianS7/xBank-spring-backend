package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.account.transactions.BankingAccountTransaction;
import com.damian.xBank.banking.account.transactions.BankingAccountTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
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

    // just check the request and pass it
    @Transactional
    public BankingAccountTransaction handleCreateTransactionRequest(
            Long fromBankingAccountId,
            BankingAccountTransactionCreateRequest request) {

        // checking for null for testing purposes since it is already validated in controller
        if (fromBankingAccountId == null) {
            throw new BankingAccountException("Banking account id cannot be null");
        }

        // check if it is a transfer to another account
        if (isTransfer(request.transactionType())) {

            // a transfer to another account must have an id
            if (request.bankingAccountId_to() == null) {
                throw new BankingAccountException("A transfer must contain the receiver id");
            }

            // check that you are no sending to same banking account
            if (fromBankingAccountId.equals(request.bankingAccountId_to())) {
                throw new BankingAccountException("You cannot transfer to the same banking account");
            }
        }

        return createTransactionByType(
                fromBankingAccountId,
                request.bankingAccountId_to(),
                request.amount(),
                request.transactionType(),
                request.description()
        );
    }

    private BankingAccountTransaction createTransactionByType(
            Long fromBankAccountId,
            Long toBankAccountId,
            BigDecimal amount,
            BankingAccountTransactionType transactionType,
            String description
    ) {
        // if its transfer
        return switch (transactionType) {
            case TRANSFER_TO -> this.createTransferTransaction(
                    fromBankAccountId,
                    toBankAccountId,
                    amount,
                    transactionType,
                    description
            );
            default -> this.validateAndCreateTransaction(fromBankAccountId, amount, transactionType, description);
        };
    }

    private BankingAccountTransaction createTransferTransaction(
            Long fromBankAccountId,
            Long toBankAccountId,
            BigDecimal amount,
            BankingAccountTransactionType transactionType,
            String description
    ) {
        BankingAccountTransaction senderTransaction = validateAndCreateTransaction(
                fromBankAccountId,
                amount,
                transactionType,
                description
        );

        // receiver
        validateAndCreateTransaction(
                toBankAccountId,
                amount,
                BankingAccountTransactionType.TRANSFER_FROM,
                "Transfer from "
                        + senderTransaction.getOwnerAccount().getCustomer().getFullName().toUpperCase()
        );
        return senderTransaction;
    }

    private BankingAccountTransaction validateAndCreateTransaction(
            Long bankingAccountId,
            BigDecimal amount,
            BankingAccountTransactionType transactionType,
            String description
    ) {
        // check if the banking account exists
        final BankingAccount bankingAccount = bankingAccountRepository.findById(bankingAccountId)
                .orElseThrow(
                        () -> new BankingAccountException("Banking Account not found")
                );

        // check if the account is not locked or closed
        if (!bankingAccount.getAccountStatus().equals(BankingAccountStatus.OPEN)) {
            throw new BankingAccountException("Banking account should be open to carry any transaction");
        }

        //
        final boolean isSpendingTransactionType =
                transactionType.equals(BankingAccountTransactionType.WITHDRAWAL)
                        || transactionType.equals(BankingAccountTransactionType.TRANSFER_TO)
                        || transactionType.equals(BankingAccountTransactionType.CARD_CHARGE);

        // ...
        if (isSpendingTransactionType) {
            // check if the customer associated to the account has the same id that the logged customer
            // get logged customer from the context
            final Customer customerLogged = (Customer) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            // check if the account belongs to the customer
            if (!bankingAccount.getCustomer().getId().equals(customerLogged.getId())) {
                throw new BankingAccountException("This accounts is not yours!");
            }

            // check if customer can afford the transaction
            if (!this.hasSufficientBalance(bankingAccount.getBalance(), amount)) {
                throw new BankingAccountException("Insufficient funds");
            }

            // deduce the amount from the balance
            bankingAccount.setBalance(
                    bankingAccount.getBalance().subtract(amount)
            );

        }

        final boolean isReceivingFundsTransactionType =
                transactionType.equals(BankingAccountTransactionType.DEPOSIT)
                        || transactionType.equals(BankingAccountTransactionType.TRANSFER_FROM);

        // if the transaction is receive to customer account
        if (isReceivingFundsTransactionType) {
            // add the amount to the balance
            bankingAccount.setBalance(
                    bankingAccount.getBalance().add(amount)
            );
        }

        return this.storeTransaction(
                bankingAccount,
                transactionType,
                amount,
                description
        );
    }

    private BankingAccountTransaction storeTransaction(
            BankingAccount bankingAccount,
            BankingAccountTransactionType transactionType,
            BigDecimal amount,
            String description
    ) {
        // we create the transaction in order to save it
        BankingAccountTransaction transaction = new BankingAccountTransaction(bankingAccount);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setDescription(description);

        // we add the transaction to the account
        bankingAccount.addAccountTransaction(transaction);

        // we save the transaction
        bankingAccountRepository.save(bankingAccount);

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
        // ...
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        final BankingAccount bankingAccount = bankingAccountRepository.findById(id).orElseThrow(
                () -> new BankingAccountException("BankingAccount cannot be found")
        );

        // check if the account to be closed belongs to this customer.
        if (!bankingAccount.getCustomer().getId().equals(customerLogged.getId())) {
            // in case CustomerRole.ADMIN it will no throw
            if (customerLogged.getRole().equals(CustomerRole.CUSTOMER)) {
                throw new BankingAccountException("You cannot close an account that is not yours");
            }
        }

        // we mark the account as closed
        bankingAccount.setAccountStatus(BankingAccountStatus.CLOSED);

        // save the data and return BankingAccount
        return bankingAccountRepository.save(bankingAccount);
    }

    private boolean isTransfer(BankingAccountTransactionType transactionType) {
        return transactionType.equals(BankingAccountTransactionType.TRANSFER_TO);
    }

    public String generateAccountNumber() {
        return faker.finance().iban();
    }
}
