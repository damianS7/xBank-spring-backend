package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.account.BankingAccountStatus;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionException;
import com.damian.xBank.banking.transactions.http.BankingAccountTransactionRequest;
import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.common.utils.AuthHelper;
import com.damian.xBank.customer.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankingTransactionAccountService {
    private final BankingAccountRepository bankingAccountRepository;
    private final BankingTransactionService bankingTransactionService;

    public BankingTransactionAccountService(
            BankingAccountRepository bankingAccountRepository,
            BankingTransactionService bankingTransactionService
    ) {
        this.bankingAccountRepository = bankingAccountRepository;
        this.bankingTransactionService = bankingTransactionService;
    }

    // handle request BankingTransactionType and determine what to do.
    public BankingTransaction processTransactionRequest(
            Long fromAccountId,
            BankingAccountTransactionRequest request
    ) {
        // BankingAccount to operate
        BankingAccount bankingAccount = bankingAccountRepository.findById(fromAccountId).orElseThrow(
                () -> new BankingAccountNotFoundException(
                        Exceptions.ACCOUNT.NOT_FOUND
                )
        );

        return switch (request.transactionType()) {
            case TRANSFER_TO -> this.transferTo(
                    bankingAccount,
                    request.toBankingAccountNumber(),
                    request.password(),
                    request.amount(),
                    request.description()
            );
            case DEPOSIT -> this.deposit(bankingAccount, request.password(), request.amount());
            default -> throw new BankingTransactionException(
                    Exceptions.TRANSACTION.INVALID_TYPE
            );
        };
    }

    // security checks before account operations
    public void validateCustomerAuthorization(
            BankingAccount bankingAccount,
            String password
    ) {
        // Customer logged
        final Customer customerLogged = AuthHelper.getLoggedCustomer();

        // if the owner of the card is not the current logged customer.
        if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
            throw new BankingAccountAuthorizationException(
                    Exceptions.ACCOUNT.ACCESS_FORBIDDEN
            );
        }

        // check password
        AuthHelper.validatePasswordOrElseThrow(password, customerLogged);
    }

    // validates all security checks before transfer
    public void validateTransferOrElseThrow(
            BankingAccount fromBankingAccount,
            BankingAccount toBankingAccount,
            BigDecimal amount
    ) {
        // check bankingAccount and toBankingAccount are not the same
        if (fromBankingAccount.getId().equals(toBankingAccount.getId())) {
            throw new BankingAccountAuthorizationException(
                    Exceptions.ACCOUNT.SAME_DESTINATION
            );
        }

        // check currency are the same
        this.checkCurrency(fromBankingAccount, toBankingAccount);

        // check the funds from the sender account
        this.checkFunds(fromBankingAccount, amount);

        // check the account status and see if can be used to operate
        this.checkAccountStatus(fromBankingAccount);
        this.checkAccountStatus(toBankingAccount);
    }

    // its check the status of the account and throws if closed or suspended
    private void checkAccountStatus(
            BankingAccount bankingAccount
    ) {
        // check account status
        final boolean isAccountClosed = bankingAccount.getAccountStatus().equals(BankingAccountStatus.CLOSED);
        final boolean isAccountSuspended = bankingAccount.getAccountStatus().equals(BankingAccountStatus.SUSPENDED);

        if (isAccountClosed) {
            throw new BankingAccountAuthorizationException(
                    Exceptions.ACCOUNT.CLOSED
            );
        }

        if (isAccountSuspended) {
            throw new BankingAccountAuthorizationException(
                    Exceptions.ACCOUNT.SUSPENDED
            );
        }
    }

    // check the funds from the account
    private void checkFunds(BankingAccount bankingAccount, BigDecimal amount) {
        if (!bankingAccount.hasEnoughFunds(amount)) {
            throw new BankingAccountAuthorizationException(
                    Exceptions.ACCOUNT.INSUFFICIENT_FUNDS
            );
        }
    }

    // check currency are the same
    private void checkCurrency(BankingAccount fromBankingAccount, BankingAccount toBankingAccount) {
        if (!fromBankingAccount.getAccountCurrency()
                               .equals(toBankingAccount.getAccountCurrency())
        ) {
            throw new BankingAccountAuthorizationException(
                    Exceptions.TRANSACTION.DIFFERENT_CURRENCY
            );
        }
    }

    // validates account status and does the transaction
    public BankingTransaction transferTo(
            BankingAccount fromBankingAccount,
            String toBankingAccountNumber,
            String password,
            BigDecimal amount,
            String description
    ) {
        // Banking account to receive funds
        final BankingAccount toBankingAccount = bankingAccountRepository
                .findByAccountNumber(toBankingAccountNumber)
                .orElseThrow(
                        () -> new BankingAccountNotFoundException(
                                Exceptions.ACCOUNT.NOT_FOUND
                        )
                );

        // check customer authorization
        this.validateCustomerAuthorization(fromBankingAccount, password);

        // check transfer is valid
        this.validateTransferOrElseThrow(fromBankingAccount, toBankingAccount, amount);

        return this.transferTo(fromBankingAccount, toBankingAccount, amount, description);
    }

    public BankingTransaction transferTo(
            BankingAccount fromBankingAccount,
            BankingAccount toBankingAccount,
            BigDecimal amount,
            String description
    ) {
        BankingTransaction fromTransaction = this.bankingTransactionService.createTransaction(
                fromBankingAccount,
                BankingTransactionType.TRANSFER_TO,
                amount,
                description
        );

        fromBankingAccount.subtractAmount(amount);
        fromTransaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);
        this.bankingTransactionService.persistTransaction(fromTransaction);

        // create transfer transaction for the receiver of the funds
        BankingTransaction toTransaction = this.bankingTransactionService.createTransaction(
                toBankingAccount,
                BankingTransactionType.TRANSFER_FROM,
                amount,
                "Transfer from " + fromBankingAccount.getOwner().getFullName()
        );

        toBankingAccount.deposit(amount);
        toTransaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);
        this.bankingTransactionService.persistTransaction(toTransaction);

        return fromTransaction;
    }

    // validates account status and does the transaction
    public BankingTransaction deposit(
            BankingAccount account,
            String password,
            BigDecimal amount
    ) {
        BankingTransaction transaction = this.bankingTransactionService.createTransaction(
                account,
                BankingTransactionType.DEPOSIT,
                amount,
                "DEPOSIT"
        );

        // check customer authorization
        this.validateCustomerAuthorization(account, password);

        // check account authorization
        this.checkAccountStatus(account);

        // if the transaction is created, deduce the amount from balance
        account.deposit(amount);

        // transaction is completed
        transaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);

        // save the transaction
        return bankingTransactionService.persistTransaction(transaction);
    }
}