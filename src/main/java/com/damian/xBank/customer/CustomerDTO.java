package com.damian.xBank.customer;

public record CustomerDTO(Long id, String email, CustomerRole role) {
    public static CustomerDTO build(Customer customer) {
        return CustomerDTO.build(customer.getId(), customer.getEmail(), customer.getRole());
    }

    public static CustomerDTO build(Long id, String email, CustomerRole role) {
        return new CustomerDTO(id, email, role);
    }
}