package com.damian.xBank.common.utils;

import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.profile.Profile;
import com.damian.xBank.customer.profile.exception.ProfileAuthorizationException;
import com.damian.xBank.customer.profile.exception.ProfileException;

public class ProfileUtils {
    public static void hasAuthorizationOrElseThrow(Profile profile, Customer customer) {
        // check if the profile belongs to the customer
        if (!profile.getCustomer().getId().equals(customer.getId())) {
            throw new ProfileAuthorizationException(ProfileException.AUTHORIZATION.ACCESS_FORBIDDEN);
        }
    }
}
