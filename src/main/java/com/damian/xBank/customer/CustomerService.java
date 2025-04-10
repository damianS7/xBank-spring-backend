package com.damian.xBank.customer;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.customer.exception.CustomerException;
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
    public Customer getCustomer(Long userId) {
        return customerRepository.findById(userId).orElseThrow(
                () -> new CustomerException("Customer not found.")
        );
    }

    public Customer createCustomer(AuthenticationRequest request) {
        return createCustomer(request.email(), request.password());
    }

    // Crea un usuario
    public Customer createCustomer(String email, String password) {

        if (emailExist(email)) {
            throw new CustomerException("Email is taken.");
        }

        return customerRepository.save(
                new Customer(
                        email,
                        bCryptPasswordEncoder.encode(password)
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

    // Modifica los datos de un usuario
    public Customer updateCustomer(CustomerUpdateRequest request) {
        // check if user is logged and token is valid
//        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        System.out.println(authToken);

        final String currentEmail = request.currentEmail();
        final String newEmail = request.newEmail();
        final String newRawPassword = request.newPassword();
        final String currentRawPassword = request.currentPassword();


        // Obtenemos el nombre del usuario logeado que envia la peticion
        Customer customer = customerRepository.findByEmail(currentEmail).orElseThrow(
                () -> new CustomerException("Customer cannot be found.")
        );

        // Antes de cambiar comprobamos que las password antiguas coincidan
        if (!bCryptPasswordEncoder.matches(currentRawPassword, customer.getPassword())) {
            throw new CustomerException("Password does not match.");
        }

        // nuevo password encriptado
        if (newRawPassword != null) {
            customer.setPassword(
                    bCryptPasswordEncoder.encode(newRawPassword)
            );
        }

        // Modificamos el usuario
        if (newEmail != null) {
            customer.setEmail(newEmail);
        }

        // Guardamos los cambios
        customerRepository.save(customer);

        return customer;
    }
}
