package com.damian.xBank.customer;

import com.damian.xBank.common.DTOBuilder;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public CustomerService(CustomerRepository customerRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.customerRepository = customerRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // Devuelve todos los usuarios existentes
    public List<CustomerDTO> getCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(
                        DTOBuilder::build
                ).toList();
    }

    // devuelve un usuario
    public Customer getCustomer(Long userId) {
        return customerRepository.findById(userId).orElseThrow(
                () -> new CustomerException("Customer not found.")
        );
    }

    public Customer createCustomer(CustomerRegistrationRequest request) {

        if (emailExist(request.email())) {
            throw new CustomerException("Email is taken.");
        }

        Customer customer = new Customer();
        customer.setEmail(request.email());
        customer.setPassword(bCryptPasswordEncoder.encode(request.password()));
        customer.getProfile().setNationalId(request.nationalId());
        customer.getProfile().setName(request.name());
        customer.getProfile().setSurname(request.surname());
        customer.getProfile().setPhone(request.phone());
        customer.getProfile().setGender(request.gender());
        customer.getProfile().setBirthdate(request.birthdate());
        customer.getProfile().setCountry(request.country());
        customer.getProfile().setAddress(request.address());
        customer.getProfile().setPostalCode(request.postalCode());
        customer.getProfile().setPhoto(request.photo());

        return customerRepository.save(customer);
    }

    // Borra un usuario
    public boolean deleteCustomer(Long userId) {
        if (!customerRepository.existsById(userId)) {
            throw new CustomerException("Customer do not exist.");
        }
        customerRepository.deleteById(userId);
        return true;
    }

    private boolean emailExist(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    // Modifica los datos de un usuario
    public Customer updateCustomer(CustomerUpdateRequest request) {
        // check if user is logged and token is valid
        final String customerEmail = ((Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getEmail();

        // We get the Customer entity so we can save at the end
        Customer customer = customerRepository.findByEmail(customerEmail).orElseThrow(
                () -> new CustomerException("Customer cannot be found.")
        );

        // Before making any changes we check that the password sent by the customer matches the one in the entity
        if (!bCryptPasswordEncoder.matches(request.currentPassword(), customer.getPassword())) {
            throw new CustomerException("Password does not match.");
        }

        // if a new password is specified we set in the customer entity
        if (request.newPassword() != null) {
            customer.setPassword(
                    bCryptPasswordEncoder.encode(request.newPassword())
            );
        }

        // Modificamos el usuario
        if (request.newEmail() != null) {
            customer.setEmail(request.newEmail());
        }

        // Guardamos los cambios
        customerRepository.save(customer);

        return customer;
    }
}
