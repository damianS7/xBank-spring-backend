package com.damian.xBank.customer;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomerDetails extends UserDetails {
    String getEmail();
    void setEmail(String email);
}
