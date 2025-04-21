package com.damian.xBank.auth.http.request;

import com.damian.xBank.customer.CustomerDTO;

public record AuthenticationResponse(
        CustomerDTO customer,
        String token) {
}

