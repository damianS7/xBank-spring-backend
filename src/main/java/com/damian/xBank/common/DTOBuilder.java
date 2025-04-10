package com.damian.xBank.common;

import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerDTO;
import com.damian.xBank.profile.Profile;
import com.damian.xBank.profile.ProfileDTO;

public class DTOBuilder {
    public static CustomerDTO build(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                customer.getProfile().toDTO()
        );
    }

    public static ProfileDTO build(Profile profile) {
        return new ProfileDTO(
                profile.getId(),
                profile.getName(),
                profile.getSurname(),
                profile.getPhone(),
                profile.getBirthdate(),
                profile.getGender(),
                profile.getPhoto(),
                profile.getAddress(),
                profile.getPostalCode(),
                profile.getCountry(),
                profile.getNationalId()
        );
    }
}
