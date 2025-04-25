package com.damian.xBank.customer;

import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.common.utils.DTOBuilder;
import com.damian.xBank.customer.exception.CustomerEmailTakenException;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import com.damian.xBank.customer.http.request.CustomerEmailUpdateRequest;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
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

    /**
     * Creates a new customer
     *
     * @param request contains the fields needed for the customer creation
     * @return the customer created
     * @throws CustomerException if another user has the email
     */
    public Customer createCustomer(CustomerRegistrationRequest request) {

        // check if the email is already taken
        if (emailExist(request.email())) {
            throw new CustomerEmailTakenException(request.email());
        }

        // we create the customer and assign the data
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
        customer.getProfile().setPhotoPath(request.photo());

        return customerRepository.save(customer);
    }

    /**
     * Deletes a customer
     *
     * @param customerId the id of the customer to be deleted
     * @return true if the customer was deleted
     * @throws CustomerException if the customer does not exist or if the logged user is not ADMIN
     */
    public boolean deleteCustomer(Long customerId) {
        // if the customer does not exist we throw an exception
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        // we delete the customer
        customerRepository.deleteById(customerId);

        // if no exception is thrown we return true
        return true;
    }

    /**
     * Returns all the customers transformed to DTO
     *
     * @return a list of CustomerDTO
     * @throws CustomerException if the logged user is not ADMIN
     */
    public List<CustomerDTO> getCustomers() {
        // we return all the customers transformed to DTO
        return customerRepository.findAll()
                .stream()
                .map(
                        DTOBuilder::build
                ).toList();
    }

    /**
     * Returns a customer
     *
     * @param customerId the id of the customer to be returned
     * @return the customer
     * @throws CustomerException if the customer does not exist or if the logged user is not ADMIN
     */
    public Customer getCustomer(Long customerId) {
        // if the customer does not exist we throw an exception
        return customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(customerId)
        );
    }

    /**
     * It checks if an email exist in the database
     *
     * @param email the email to be checked
     * @return true if the email exists, false otherwise
     */
    private boolean emailExist(String email) {
        // we search the email in the database
        return customerRepository.findByEmail(email).isPresent();
    }

    /**
     * It updates the email of a customer
     *
     * @param request the request body that contains the current password and the new email
     * @return the customer updated
     * @throws CustomerException if the password does not match, or if the customer does not exist
     */
    public Customer updateEmail(CustomerEmailUpdateRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer loggedCustomer = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // we get the Customer entity so we can save at the end
        Customer customer = customerRepository.findByEmail(loggedCustomer.getEmail()).orElseThrow(
                () -> new CustomerNotFoundException(loggedCustomer.getEmail())
        );

        // Before making any changes we check that the password sent by the customer matches the one in the entity
        if (!bCryptPasswordEncoder.matches(request.currentPassword(), customer.getPassword())) {
            throw new PasswordMismatchException();
        }

        // if the email is not null we modify in the customer
        if (request.newEmail() != null) {
            customer.setEmail(request.newEmail());
        }

        // save the changes
        return customerRepository.save(customer);
    }
}
