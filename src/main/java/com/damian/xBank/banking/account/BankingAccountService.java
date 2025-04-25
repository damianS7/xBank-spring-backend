package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountException;
import com.damian.xBank.banking.account.exception.BankingAccountInsufficientFundsException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.account.transactions.BankingAccountTransaction;
import com.damian.xBank.banking.account.transactions.BankingAccountTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import net.datafaker.Faker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

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

    /**
     * Handles the creation of a banking account transaction.
     * This method checks the validity of the transaction request and processes it.
     *
     * @param fromBankingAccountId the ID of the source banking account
     * @param request              the transaction request containing details of the transaction
     * @return the created BankingAccountTransaction
     * @throws BankingAccountException if any validation fails
     */
    @Transactional(rollbackFor = BankingAccountException.class)
    public BankingAccountTransaction handleCreateTransactionRequest(
            Long fromBankingAccountId,
            BankingAccountTransactionCreateRequest request) {

        // Validate that the source banking account ID is not null
        if (fromBankingAccountId == null) {
            throw new BankingAccountException("Banking account id cannot be null");
        }

        // validate that the request is not null
        if (request == null) {
            throw new BankingAccountException("Request cannot be null");
        }

        // Check if the transaction is a transfer
        if (isTransfer(request.transactionType())) {

            // Validate that the transfer request contains a receiver ID
            if (request.bankingAccountId_to() == null) {
                throw new BankingAccountException("A transfer must contain the receiver id");
            }

            // Ensure that the transfer is not to the same banking account
            if (fromBankingAccountId.equals(request.bankingAccountId_to())) {
                throw new BankingAccountException("You cannot transfer to the same banking account");
            }
        }

        // Generate and return the transaction
        return generateTransaction(
                fromBankingAccountId,
                request.bankingAccountId_to(),
                request.amount(),
                request.transactionType(),
                request.description()
        );
    }

    /**
     * Creates a BankingAccountTransaction and persists it. If the transaction is of type
     * {@link BankingAccountTransactionType#TRANSFER_TO}, it also creates and persists a second transaction of type
     * {@link BankingAccountTransactionType#TRANSFER_FROM} for the destination account.
     *
     * @param fromBankAccountId the ID of the source banking account
     * @param toBankAccountId   the ID of the destination banking account, or null if not a transfer
     * @param amount            the amount of the transaction
     * @param transactionType   the type of the transaction
     * @param description       the description of the transaction
     * @return the created BankingAccountTransaction
     */
    private BankingAccountTransaction generateTransaction(
            Long fromBankAccountId,
            Long toBankAccountId,
            BigDecimal amount,
            BankingAccountTransactionType transactionType,
            String description
    ) {

        // Create a transaction for the source account
        BankingAccountTransaction fromTransaction = createTransaction(
                fromBankAccountId,
                amount,
                transactionType,
                description
        );

        // If the transaction is a transfer, create a corresponding transaction for the destination account
        if (transactionType.equals(BankingAccountTransactionType.TRANSFER_TO)) {
            BankingAccountTransaction toTransaction = createTransaction(
                    toBankAccountId,
                    amount,
                    BankingAccountTransactionType.TRANSFER_FROM,
                    "Transfer from "
                            + fromTransaction.getOwnerAccount().getCustomer().getFullName().toUpperCase()
            );
            // Persist the transaction for the destination account
            storeTransaction(toTransaction);
        }

        // Persist and return the transaction for the source account
        return storeTransaction(fromTransaction);
    }

    /**
     * Creates a BankingAccountTransaction and configures it with the given parameters.
     * This method also performs the following checks:
     * - the banking account exists
     * - the account is not locked or closed
     * - the customer associated to the account is the same as the logged customer
     * - the customer can afford the transaction (if it's a spending transaction)
     * - the balance is updated according to the transaction type
     *
     * @param fromBankAccountId the ID of the source banking account
     * @param amount            the amount of the transaction
     * @param transactionType   the type of the transaction
     * @param description       the description of the transaction
     * @return the created BankingAccountTransaction
     */
    private BankingAccountTransaction createTransaction(
            Long fromBankAccountId,
            BigDecimal amount,
            BankingAccountTransactionType transactionType,
            String description
    ) {
        // check if the banking account exists
        final BankingAccount bankingAccount = bankingAccountRepository.findById(fromBankAccountId)
                .orElseThrow(
                        () -> new BankingAccountNotFoundException(fromBankAccountId)
                );

        // check if the account is not locked or closed
        if (!bankingAccount.getAccountStatus().equals(BankingAccountStatus.OPEN)) {
            throw new BankingAccountException("Banking account is closed. Transactions are not allowed.");
        }

        // check if the transaction is a spending transaction
        final boolean isSpendingTransactionType =
                transactionType.equals(BankingAccountTransactionType.WITHDRAWAL)
                        || transactionType.equals(BankingAccountTransactionType.TRANSFER_TO)
                        || transactionType.equals(BankingAccountTransactionType.CARD_CHARGE);

        // if it's a spending transaction
        if (isSpendingTransactionType) {
            // get logged customer from the context
            final Customer customerLogged = (Customer) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            // check if the account belongs to the customer
            if (!bankingAccount.getCustomer().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingAccountAuthorizationException();
            }

            // check if customer can afford the transaction
            if (!this.hasSufficientBalance(bankingAccount.getBalance(), amount)) {
                throw new BankingAccountInsufficientFundsException();
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

        // create the transaction
        BankingAccountTransaction transaction = new BankingAccountTransaction(bankingAccount);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setDescription(description);

        return transaction;
    }

    /**
     * Stores a banking account transaction by adding it to the owner's account and persisting the account.
     *
     * @param transaction the banking account transaction to store
     * @return the stored banking account transaction
     */
    private BankingAccountTransaction storeTransaction(BankingAccountTransaction transaction) {
        // Add the transaction to the owner's account
        transaction.getOwnerAccount().addAccountTransaction(transaction);

        // Persist the owner's account with the new transaction
        bankingAccountRepository.save(transaction.getOwnerAccount());

        // Return the stored transaction
        return transaction;
    }

    // returns true if the operation can be carried
    public boolean hasSufficientBalance(BigDecimal balance, BigDecimal amountToSpend) {
        // if its 0 then balance is equal to the amount willing to spend
        // if its 1 then balance is greater than the amount willing to spend
        return balance.compareTo(amountToSpend) >= 0;
    }

    // return all the BankingsAccount that belongs to customerId
    public Set<BankingAccount> getCustomerBankingAccounts(Long customerId) {
        return bankingAccountRepository.findByCustomer_Id(customerId);
    }

    public BankingAccount openBankingAccount(BankingAccountOpenRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer customerLogged = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // we get the Customer entity so we can save at the end
        final Customer customer = customerRepository.findByEmail(customerLogged.getEmail()).orElseThrow(
                () -> new CustomerNotFoundException(customerLogged.getEmail())
        );

        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setCustomer(customer);
        bankingAccount.setAccountType(request.accountType());
        bankingAccount.setAccountCurrency(request.accountCurrency());
        bankingAccount.setAccountNumber(this.generateAccountNumber());

        return bankingAccountRepository.save(bankingAccount);
    }

    public BankingAccount closeBankingAccount(Long bankingAccountId) {
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
            if (!bankingAccount.getCustomer().getId().equals(customerLogged.getId())) {
                // banking account does not belong to this customer
                throw new BankingAccountAuthorizationException();
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
