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
import com.damian.xBank.customer.profile.exception.ProfileNotFoundException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// TODO CHECK FOR NULLS WHEN BUILDING
public class DTOBuilder {
    public static CustomerDTO build(Customer customer) {
        ProfileDTO profileDTO = Optional.ofNullable(customer.getProfile().toDTO())
                                        .orElseThrow(ProfileNotFoundException::new);

        Set<BankingAccountDTO> bankingAccountsDTO = Optional.ofNullable(customer.getBankingAccounts())
                                                            .orElseGet(Collections::emptySet)
                                                            .stream()
                                                            .map(BankingAccount::toDTO)
                                                            .collect(Collectors.toSet());

        return new CustomerDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                profileDTO,
                bankingAccountsDTO
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
        BankingCardDTO bankingCardDTO =
                bankingAccount.getBankingCard() != null ? bankingAccount.getBankingCard().toDTO() : null;

        Set<BankingAccountTransactionDTO> bankingTransactionsDTO = Optional
                .ofNullable(bankingAccount.getAccountTransactions())
                .orElseGet(Collections::emptySet)
                .stream()
                .map(BankingAccountTransaction::toDTO)
                .collect(Collectors.toSet());

        return new BankingAccountDTO(
                bankingAccount.getId(),
                bankingAccount.getAccountNumber(),
                bankingAccount.getBalance(),
                bankingAccount.getAccountType(),
                bankingAccount.getAccountCurrency(),
                bankingAccount.getAccountStatus(),
                bankingAccount.getCreatedAt(),
                bankingCardDTO,
                bankingTransactionsDTO
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
