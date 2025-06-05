package com.damian.xBank.common.utils;

import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardLockStatus;
import com.damian.xBank.banking.card.BankingCardStatus;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.customer.Customer;

public class BankingCardAuthorizationHelper {
    private Customer customer;
    private BankingCard card;

    public static BankingCardAuthorizationHelper authorize(Customer customer, BankingCard card) {
        BankingCardAuthorizationHelper helper = new BankingCardAuthorizationHelper();
        helper.card = card;
        helper.customer = customer;
        return helper;
    }

    /**
     * Check if the BankingCard belongs to this customer
     *
     * @return BankingCardAuthorizationHelper
     */
    public BankingCardAuthorizationHelper checkOwner() {
        if (!card.getOwner().getId().equals(customer.getId())) {
            // banking card does not belong to this customer
            throw new BankingCardAuthorizationException(
                    Exceptions.CARD.ACCESS_FORBIDDEN
            );
        }
        return this;
    }


    /**
     * Check if card is not disabled or locked
     *
     * @return BankingCardAuthorizationHelper
     */
    public BankingCardAuthorizationHelper checkStatus() {
        // check card status
        final boolean isCardDisabled = card.getCardStatus().equals(BankingCardStatus.DISABLED);

        if (isCardDisabled) {
            throw new BankingCardAuthorizationException(
                    Exceptions.CARD.DISABLED
            );
        }

        // check lock status
        final boolean isCardLocked = card.getLockStatus().equals(BankingCardLockStatus.LOCKED);
        if (isCardLocked) {
            throw new BankingCardAuthorizationException(
                    Exceptions.CARD.LOCKED
            );
        }
        return this;
    }

    public BankingCardAuthorizationHelper checkPIN(String PIN) {
        // check card pin
        if (!card.getCardPin().equals(PIN)) {
            throw new BankingCardAuthorizationException(
                    Exceptions.CARD.INVALID_PIN
            );
        }
        return this;
    }
}
