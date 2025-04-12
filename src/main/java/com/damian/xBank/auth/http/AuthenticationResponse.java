package com.damian.xBank.auth.http;

import com.damian.xBank.customer.CustomerDTO;

public record AuthenticationResponse(CustomerDTO customer, String token) {
}

