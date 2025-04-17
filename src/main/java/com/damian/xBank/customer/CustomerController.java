package com.damian.xBank.customer;

import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/api/v1")
@RestController
public class CustomerController {
    private final CustomerService customerService;
    private final BankingAccountService bankingAccountService;

    @Autowired
    public CustomerController(CustomerService customerService, BankingAccountService bankingAccountService) {
        this.customerService = customerService;
        this.bankingAccountService = bankingAccountService;
    }

    // endpoint to receive certain customer
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Customer customer = customerService.getCustomer(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customer.toDTO());
    }

    // endpoint to modify customers
    @PutMapping("/customer/{id}")
    public ResponseEntity<?> updateUser(@Validated @RequestBody CustomerUpdateRequest request) {
        Customer customer = customerService.updateCustomer(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customer.toDTO());
    }

    // endpoint to receive all BankingAccounts from user
    @GetMapping("/customer/{customer_id}/banking_accounts")
    public ResponseEntity<?> getCustomerBankingAccounts(@PathVariable Long customerId) {
        Set<BankingAccountDTO> bankingAccounts = bankingAccountService.getBankingAccounts(customerId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccounts);
    }
}

