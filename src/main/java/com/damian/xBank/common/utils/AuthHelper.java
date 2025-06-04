package com.damian.xBank.common.utils;

import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardStatus;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthHelper {
    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public static void validatePasswordOrElseThrow(String rawPassword, Customer customer) {
        if (!bCryptPasswordEncoder.matches(rawPassword, customer.getAuth().getPassword())) {
            throw new PasswordMismatchException(PasswordMismatchException.PASSWORD_MISMATCH);
        }
    }

    public static Customer getLoggedCustomer() {
        return (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static boolean isAdmin(Customer customer) {
        return customer.getRole().equals(CustomerRole.ADMIN);
    }

    // Account

    // Card
    public static void authorizedOrElseThrow(Customer customer, BankingCard card, String password) {
        // if the logged customer is not admin
        if (!customer.getRole().equals(CustomerRole.ADMIN)) {
            // check if the account to be closed belongs to this customer.
            AuthHelper.checkCardOwnership(card, customer);

            // check password
            AuthHelper.validatePasswordOrElseThrow(password, customer);
        }
    }

    // check ownership of a BankingCard
    private static void checkCardOwnership(BankingCard bankingCard, Customer customer) {
        if (!bankingCard.getCardOwner().getId().equals(customer.getId())) {
            // banking card does not belong to this customer
            throw new BankingCardAuthorizationException(
                    Exceptions.CARD.ACCESS_FORBIDDEN
            );
        }
    }

    // check card is not disabled
    public static void cardUsableOrThrow(BankingCard card) {
        // suspended accounts can only change status by admin
        if (card.getCardStatus().equals(BankingCardStatus.DISABLED)) {
            throw new BankingCardAuthorizationException(Exceptions.CARD.DISABLED);
        }
    }
}
