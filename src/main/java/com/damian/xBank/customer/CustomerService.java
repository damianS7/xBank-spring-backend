package com.damian.xBank.customer;

import com.damian.xBank.auth.Auth;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
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
                        CustomerDTO::build
                ).toList();
    }

    // devuelve un usuario
    public Customer getCustomer(Long userId) throws CustomerException {
        return customerRepository.findById(userId).orElseThrow(
                () -> new CustomerException("Customer not found.")
        );
    }

    // Crea un usuario
    public Customer createCustomer(CustomerRegistrationRequest request) {

        if (emailExist(request.email())) {
            throw new CustomerException("Email is taken.");
        }

        return customerRepository.save(
                new Customer(
                        request.email(),
                        bCryptPasswordEncoder.encode(request.password())
                )
        );
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

    // PARA REVIEW
    public Customer updateCustomer(CustomerUpdateRequest request) {
        return this.updateCustomer(request.email(), request.actualPassword(), request.newPassword());
    }

    // Modifica los datos de un usuario
    public Customer updateCustomer(String email, String actualRawPassword, String newRawPassword) throws CustomerException {
        //String currentCustomername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // check if user is logged and token is valid
        // Obtenemos el nombre del usuario logeado que envia la peticion
        Customer customer = customerRepository.findByEmail(email).orElseThrow(
                () -> new CustomerException("Customer cannot be found.")
        );

        // Antes de cambiar comprobamos que las password antiguas coincidan
        if (!bCryptPasswordEncoder.matches(actualRawPassword, customer.getPassword())) {
            throw new CustomerException("Password does not match.");
        }

        // nuevo password encriptado
        String encodedNewPassword = bCryptPasswordEncoder.encode(newRawPassword);

        // Modificamos el usuario
        customer.setEmail(email);
//        customer.setPassword(encodedNewPassword);
        customerRepository.save(customer);

        // Modificamos el password
//        Auth customerAuth = authRepository.findByCustomerId(customer.getId());
//        customerAuth.setPassword(encodedNewPassword);
//        customer.setPassword
        return customer;
    }
}
