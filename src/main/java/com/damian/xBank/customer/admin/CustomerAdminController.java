package com.damian.xBank.customer.admin;

import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerService;
import com.damian.xBank.customer.dto.CustomerDTO;
import com.damian.xBank.customer.dto.CustomerDTOMapper;
import com.damian.xBank.customer.dto.CustomerWithAllDataDTO;
import com.damian.xBank.customer.http.request.CustomerEmailUpdateRequest;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class CustomerAdminController {
    private final CustomerService customerService;
    private final BankingAccountService bankingAccountService;

    @Autowired
    public CustomerAdminController(CustomerService customerService, BankingAccountService bankingAccountService) {
        this.customerService = customerService;
        this.bankingAccountService = bankingAccountService;
    }

    // endpoint to receive all customers
    @GetMapping("/admin/customers")
    public ResponseEntity<?> getCustomers(
            @PageableDefault(size = 2, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Customer> customers = customerService.getCustomers(pageable);
        Page<CustomerDTO> customerDTO = CustomerDTOMapper.toCustomerDTOPage(customers);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }

    // endpoint to receive certain customer
    @GetMapping("/admin/customers/{id}")
    public ResponseEntity<?> getCustomer(
            @PathVariable @Positive
            Long id
    ) {
        Customer customer = customerService.getCustomer(id);
        CustomerWithAllDataDTO customerDTO = CustomerDTOMapper.toCustomerWithAllDataDTO(customer);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }

    // endpoint to delete a customer
    @DeleteMapping("/admin/customers/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable @Positive
            Long id
    ) {
        customerService.deleteCustomer(id);

        // returns 204
        return ResponseEntity.noContent().build();
    }

    // endpoint to update customer email
    @PatchMapping("/admin/customers/{id}/email")
    public ResponseEntity<?> updateCustomerEmail(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            CustomerEmailUpdateRequest request
    ) {
        Customer customer = customerService.updateEmail(id, request.newEmail());
        CustomerDTO customerDTO = CustomerDTOMapper.toCustomerDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }
}

