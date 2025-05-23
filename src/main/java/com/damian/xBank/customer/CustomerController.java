package com.damian.xBank.customer;

import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.customer.http.request.CustomerEmailUpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    // endpoint to modify customer email
    @PatchMapping("/customers/email")
    public ResponseEntity<?> updateCustomerEmail(
            @Validated @RequestBody
            CustomerEmailUpdateRequest request) {
        Customer customer = customerService.updateEmail(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customer.toDTO());
    }

    // endpoint to receive certain customer
    @GetMapping("/admin/customers/{id}")
    public ResponseEntity<?> getCustomer(
            @PathVariable @NotNull @Positive
            Long id) {
        Customer customer = customerService.getCustomer(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customer.toDTO());
    }

    // endpoint to receive all BankingAccounts from user
    @GetMapping("/admin/customers/{id}/banking/accounts")
    public ResponseEntity<?> getBankingAccounts(
            @PathVariable @NotNull @Positive
            Long id) {
        Set<BankingAccountDTO> bankingAccounts = bankingAccountService
                .getCustomerBankingAccountsDTO(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccounts);
    }

    // endpoint to delete a customer
    @DeleteMapping("/admin/customers/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable @NotNull @Positive
            Long id) {
        customerService.deleteCustomer(id);

        // returns 204
        return ResponseEntity.noContent().build();
    }
}

