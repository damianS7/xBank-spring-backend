package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.transactions.exception.BankingTransactionAuthorizationException;
import com.damian.xBank.banking.transactions.exception.BankingTransactionNotFoundException;
import com.damian.xBank.banking.transactions.http.BankingTransactionUpdateStatusRequest;
import com.damian.xBank.common.utils.AuthUtils;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class BankingTransactionService {
    private final BankingTransactionRepository bankingTransactionRepository;

    public BankingTransactionService(
            BankingTransactionRepository bankingTransactionRepository
    ) {
        this.bankingTransactionRepository = bankingTransactionRepository;
    }

    public Page<BankingTransaction> getBankingCardTransactions(Long bankingCardId, Pageable pageable) {
        return bankingTransactionRepository.findByBankingCardId(bankingCardId, pageable);
    }

    public Page<BankingTransaction> getBankingAccountTransactions(Long accountId, Pageable pageable) {
        return bankingTransactionRepository.findByBankingAccountId(accountId, pageable);
    }

    public BankingTransaction createTransaction(
            BankingCard fromBankingCard,
            BankingTransactionType transactionType,
            BigDecimal amount,
            String description
    ) {
        return this.createTransaction(
                fromBankingCard.getAssociatedBankingAccount(),
                transactionType,
                amount,
                description
        );
    }

    public BankingTransaction createTransaction(
            BankingAccount bankingAccount,
            BankingTransactionType transactionType,
            BigDecimal amount,
            String description
    ) {
        BankingTransaction transaction = new BankingTransaction(bankingAccount);
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
    public BankingTransaction persistTransaction(BankingTransaction transaction) {
        final BankingAccount bankingAccount = transaction.getAssociatedBankingAccount();
        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());

        // Add the transaction to the owners account
        bankingAccount.addAccountTransaction(transaction);

        // Persist the owner's account with the new transaction
        // Return the stored transaction
        return bankingTransactionRepository.save(transaction);
    }


    // TODO only admin can do this
    public BankingTransaction updateTransactionStatus(
            Long bankingTransactionId,
            BankingTransactionUpdateStatusRequest request
    ) {
        // Customer logged
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // if the logged customer is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // banking transaction does not belong to this customer
            throw new BankingTransactionAuthorizationException(
                    BankingTransactionAuthorizationException.TRANSACTION_NOT_BELONG_TO_CUSTOMER
            );
        }

        // Banking account to be closed
        final BankingTransaction bankingTransaction = bankingTransactionRepository
                .findById(bankingTransactionId)
                .orElseThrow(
                        () -> new BankingTransactionNotFoundException(
                                BankingTransactionNotFoundException.TRANSACTION_NOT_FOUND
                        )
                );


        // we mark the account as closed
        bankingTransaction.setTransactionStatus(request.transactionStatus());

        // we change the updateAt timestamp field
        bankingTransaction.setUpdatedAt(Instant.now());

        // save the data and return BankingAccount
        return bankingTransactionRepository.save(bankingTransaction);
    }
}
