package com.damian.xBank.customer;

import com.damian.xBank.common.http.response.ApiResponse;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1")
@RestController
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
    }

    // endpoint que devuelve todos los usuarios
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<?>> getUsers() {
        List<CustomerDTO> customers = customerService.getCustomers();
        ApiResponse<?> response = ApiResponse.success(
                customers,
                HttpStatus.OK
        );
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }

    // endpoint que devuelve los datos de un usuario
    @GetMapping("/customer/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getUser(@PathVariable Long id) {
        CustomerDTO customerDTO = customerService.getCustomer(id).toDTO();
        ApiResponse<CustomerDTO> response = ApiResponse.success(
                customerDTO,
                HttpStatus.OK
        );
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }

    // endpoint para modificar usuarios
    @PutMapping("/customer/{id}")
    public ResponseEntity<?> updateUser(@Validated @RequestBody CustomerUpdateRequest request) {
        CustomerDTO customerDTO = customerService.updateCustomer(request).toDTO();
        ApiResponse<CustomerDTO> response = ApiResponse.success(
                customerDTO,
                HttpStatus.OK
        );
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }

    // endpoint para borrado de usuario
    @DeleteMapping("/customer/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        ApiResponse<?> response = ApiResponse.success(
                "Customer deleted.",
                HttpStatus.ACCEPTED
        );
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }

    // endpoint para nuevos usuarios (registro)
    @PostMapping("/customer")
    public ResponseEntity<?> register(@Validated @RequestBody CustomerRegistrationRequest request) {
        Customer customer = customerService.createCustomer(request);
        ApiResponse<?> response = ApiResponse.success(
                customer.toDTO(),
                HttpStatus.CREATED
        );
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }
}

