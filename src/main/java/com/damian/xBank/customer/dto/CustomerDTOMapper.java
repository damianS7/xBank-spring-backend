package com.damian.xBank.customer.dto;

import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.banking.account.BankingAccountDTOMapper;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.profile.ProfileDTO;
import com.damian.xBank.customer.profile.ProfileDTOMapper;
import com.damian.xBank.customer.profile.exception.ProfileNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomerDTOMapper {
    public static CustomerDTO toCustomerDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithProfileDTO toCustomerWithProfileDTO(Customer customer) {
        return new CustomerWithProfileDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                ProfileDTOMapper.toProfileDTO(customer.getProfile()),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static CustomerWithAllDataDTO toCustomerWithAllDataDTO(Customer customer) {
        ProfileDTO profileDTO = Optional.ofNullable(ProfileDTOMapper.toProfileDTO(customer.getProfile()))
                                        .orElseThrow(() -> new ProfileNotFoundException(
                                                ProfileNotFoundException.NOT_FOUND));

        Set<BankingAccountDTO> bankingAccountsDTO = Optional.ofNullable(customer.getBankingAccounts())
                                                            .orElseGet(Collections::emptySet)
                                                            .stream()
                                                            .map(BankingAccountDTOMapper::toBankingAccountDTO)
                                                            .collect(Collectors.toSet());

        return new CustomerWithAllDataDTO(
                customer.getId(),
                customer.getEmail(),
                customer.getRole(),
                profileDTO,
                bankingAccountsDTO,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static List<CustomerDTO> toCustomerDTOList(List<Customer> customers) {
        return customers
                .stream()
                .map(
                        CustomerDTOMapper::toCustomerDTO
                ).toList();
    }
}
