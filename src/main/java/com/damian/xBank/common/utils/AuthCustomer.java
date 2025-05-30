package com.damian.xBank.common.utils;

import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthCustomer {
    public static boolean isPasswordCorrect(String rawPassword, String hashedPassword) {
        final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.matches(rawPassword, hashedPassword);
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
}
