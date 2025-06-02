package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.account.BankingAccountStatus;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionException;
import com.damian.xBank.banking.transactions.http.BankingAccountTransactionRequest;
import com.damian.xBank.common.utils.AuthUtils;
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
                        BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
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
                    BankingTransactionException.INVALID_TRANSACTION_TYPE
            );
        };
    }

    // security checks before account operations
    public void validateCustomerAuthorization(
            BankingAccount bankingAccount,
            String password
    ) {
        // Customer logged
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // if the owner of the card is not the current logged customer.
        if (!bankingAccount.getOwner().getId().equals(customerLogged.getId())) {
            throw new BankingAccountAuthorizationException(
                    BankingAccountAuthorizationException.ACCOUNT_NOT_BELONG_TO_CUSTOMER
            );
        }

        // check password
        AuthUtils.validatePasswordOrElseThrow(password, customerLogged);
    }

    // security checks before account operations
    public void validateAccountAuthorization(
            BankingAccount bankingAccount
    ) {
        // check account status
        final boolean isAccountClosed = bankingAccount.getAccountStatus().equals(BankingAccountStatus.CLOSED);
        final boolean isAccountSuspended = bankingAccount.getAccountStatus().equals(BankingAccountStatus.SUSPENDED);

        if (isAccountClosed) {
            throw new BankingAccountAuthorizationException(
                    BankingAccountAuthorizationException.ACCOUNT_CLOSED
            );
        }

        if (isAccountSuspended) {
            throw new BankingAccountAuthorizationException(
                    BankingAccountAuthorizationException.ACCOUNT_SUSPENDED
            );
        }
    }

    public void validateAccountFunds(BankingAccount account, BigDecimal amount) {
        if (!account.hasEnoughFunds(amount)) {
            throw new BankingAccountAuthorizationException(
                    BankingAccountAuthorizationException.INSUFFICIENT_FUNDS
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
                                BankingAccountNotFoundException.ACCOUNT_NOT_FOUND
                        )
                );

        // check bankingAccount and toBankingAccount are not the same
        if (fromBankingAccount.getId().equals(toBankingAccount.getId())) {
            throw new BankingAccountAuthorizationException(
                    BankingAccountAuthorizationException.TRANSFER_TO_SAME_ACCOUNT
            );
        }

        // TODO check currency are the same between accounts

        // check customer authorization
        this.validateCustomerAuthorization(fromBankingAccount, password);

        // check account authorization
        this.validateAccountAuthorization(fromBankingAccount);

        // check destination account authorization
        this.validateAccountAuthorization(toBankingAccount);

        // check balance
        this.validateAccountFunds(fromBankingAccount, amount);

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
        this.validateAccountAuthorization(account);

        // if the transaction is created, deduce the amount from balance
        account.deposit(amount);

        // transaction is completed
        transaction.setTransactionStatus(BankingTransactionStatus.COMPLETED);

        // save the transaction
        return bankingTransactionService.persistTransaction(transaction);
    }
}