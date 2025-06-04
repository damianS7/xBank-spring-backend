package com.damian.xBank.common.utils;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountStatus;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardStatus;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.profile.Profile;
import com.damian.xBank.customer.profile.exception.ProfileAuthorizationException;

public class AuthorizationHelper {

    public static void authorizedOrElseThrow(Customer customer, Profile profile, String password) {
        // check if the profile belongs to the customer
        if (!profile.getCustomer().getId().equals(customer.getId())) {
            throw new ProfileAuthorizationException(Exceptions.PROFILE.ACCESS_FORBIDDEN);
        }

        // check password
        AuthHelper.validatePasswordOrElseThrow(customer, password);
    }

    public static void authorizedOrElseThrow(Customer customer, BankingAccount account, String password) {
        if (!customer.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            checkAccountOwnership(customer, account);

            // check password
            AuthHelper.validatePasswordOrElseThrow(customer, password);
        }
    }

    // check ownership of a BankingAccount
    public static void checkAccountOwnership(Customer customer, BankingAccount bankingAccount) {
        // check if the account to be closed belongs to this customer.
        if (!bankingAccount.getOwner().getId().equals(customer.getId())) {
            // banking account does not belong to this customer
            throw new BankingAccountAuthorizationException(
                    Exceptions.ACCOUNT.ACCESS_FORBIDDEN
            );
        }
    }

    // check account is not suspended
    public static void accountUsableOrElseThrow(BankingAccount bankingAccount) {
        // suspended accounts can only change status by admin
        if (bankingAccount.getAccountStatus().equals(BankingAccountStatus.SUSPENDED)) {
            throw new BankingAccountAuthorizationException(Exceptions.ACCOUNT.SUSPENDED);
        }
    }

    public static void authorizedOrElseThrow(Customer customer, BankingCard card, String password) {
        // check if the account to be closed belongs to this customer.
        checkCardOwnership(customer, card);

        // check password
        AuthHelper.validatePasswordOrElseThrow(customer, password);
    }

    // check ownership of a BankingCard
    private static void checkCardOwnership(Customer customer, BankingCard bankingCard) {
        if (!bankingCard.getCardOwner().getId().equals(customer.getId())) {
            // banking card does not belong to this customer
            throw new BankingCardAuthorizationException(
                    Exceptions.CARD.ACCESS_FORBIDDEN
            );
        }
    }

    // check card is not disabled
    public static void cardUsableOrElseThrow(BankingCard card) {
        // suspended accounts can only change status by admin
        if (card.getCardStatus().equals(BankingCardStatus.DISABLED)) {
            throw new BankingCardAuthorizationException(Exceptions.CARD.DISABLED);
        }
    }
}
