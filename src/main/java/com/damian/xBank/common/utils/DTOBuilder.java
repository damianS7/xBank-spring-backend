package com.damian.xBank.common.utils;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.banking.account.transactions.BankingAccountTransaction;
import com.damian.xBank.banking.account.transactions.BankingAccountTransactionDTO;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardDTO;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerDTO;
import com.damian.xBank.customer.profile.Profile;
import com.damian.xBank.customer.profile.ProfileDTO;

import java.util.stream.Collectors;

public class DTOBuilder {
    public static CustomerDTO build(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                customer.getProfile().toDTO(),
                customer.getBankingAccounts().stream().map(
                        BankingAccount::toDTO
                ).collect(Collectors.toSet())
        );
    }

    public static ProfileDTO build(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getName(),
                profile.getSurname(),
                profile.getPhone(),
                profile.getBirthdate(),
                profile.getGender(),
                profile.getPhotoPath(),
                profile.getAddress(),
                profile.getPostalCode(),
                profile.getCountry(),
                profile.getNationalId()
        );
    }

    public static BankingAccountDTO build(BankingAccount bankingAccount) {
        return new BankingAccountDTO(
                bankingAccount.getId(),
                bankingAccount.getAccountNumber(),
                bankingAccount.getBalance(),
                bankingAccount.getAccountType(),
                bankingAccount.getAccountCurrency(),
                bankingAccount.getAccountStatus(),
                bankingAccount.getCreatedAt(),
                bankingAccount.getAccountTransactions().stream().map(
                        BankingAccountTransaction::toDTO
                ).collect(Collectors.toSet())
        );
    }

    public static BankingAccountTransactionDTO build(BankingAccountTransaction accountTransaction) {
        return new BankingAccountTransactionDTO(
                accountTransaction.getId(),
                accountTransaction.getAmount(),
                accountTransaction.getTransactionType(),
                accountTransaction.getTransactionStatus(),
                accountTransaction.getDescription(),
                accountTransaction.getCreatedAt()
        );
    }

    public static BankingCardDTO build(BankingCard bankingCard) {
        return new BankingCardDTO(
                bankingCard.getId(),
                bankingCard.getCardNumber(),
                bankingCard.getCardType(),
                bankingCard.getCardStatus()
        );
    }
}
