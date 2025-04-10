package com.damian.xBank.customer;

import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
    }

    // endpoint que devuelve los datos de un usuario
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Customer customer = customerService.getCustomer(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customer.toDTO());
    }

    // endpoint para modificar usuarios
    @PutMapping("/customer/{id}")
    public ResponseEntity<?> updateUser(@Validated @RequestBody CustomerUpdateRequest request) {
        Customer customer = customerService.updateCustomer(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customer.toDTO());
    }

}

