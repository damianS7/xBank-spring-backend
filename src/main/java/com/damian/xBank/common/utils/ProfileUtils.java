package com.damian.xBank.common.utils;

import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.profile.Profile;
import com.damian.xBank.customer.profile.exception.ProfileAuthorizationException;

public class ProfileUtils {
    public static void hasAuthorizationOrElseThrow(Profile profile, Customer customer) {
        // check if the profile belongs to the customer
        if (!profile.getCustomer().getId().equals(customer.getId())) {
            throw new ProfileAuthorizationException(Exceptions.PROFILE.ACCESS_FORBIDDEN);
        }
    }
}
