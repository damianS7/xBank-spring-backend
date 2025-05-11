package com.damian.xBank.customer;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.banking.account.BankingAccountDTOMapper;
import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.customer.dto.CustomerDTO;
import com.damian.xBank.customer.dto.CustomerDTOMapper;
import com.damian.xBank.customer.dto.CustomerWithAllDataDTO;
import com.damian.xBank.customer.dto.CustomerWithProfileDTO;
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

    // endpoint to receive logged customer
    @GetMapping("/customers/me")
    public ResponseEntity<?> getLoggedCustomerData() {
        Customer customer = customerService.getLoggedCustomer();
        CustomerWithProfileDTO dto = CustomerDTOMapper.toCustomerWithProfileDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dto);
    }

    // endpoint to modify customer email
    @PatchMapping("/customers/email")
    public ResponseEntity<?> updateCustomerEmail(
            @Validated @RequestBody
            CustomerEmailUpdateRequest request
    ) {
        Customer customer = customerService.updateEmail(request);
        CustomerDTO customerDTO = CustomerDTOMapper.toCustomerDTO(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }

    // endpoint to receive certain customer
    @GetMapping("/admin/customers/{id}")
    public ResponseEntity<?> getCustomer(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        Customer customer = customerService.getCustomer(id);
        CustomerWithAllDataDTO customerDTO = CustomerDTOMapper.toCustomerWithAllDataDTO(customer);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }

    // endpoint to receive all BankingAccounts from user
    @GetMapping("/admin/customers/{id}/banking/accounts")
    public ResponseEntity<?> getBankingAccounts(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        Set<BankingAccount> bankingAccounts = bankingAccountService
                .getCustomerBankingAccounts(id);

        Set<BankingAccountDTO> bankingAccountsDTO = BankingAccountDTOMapper
                .toBankingAccountSetDTO(bankingAccounts);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountsDTO);
    }

    // endpoint to delete a customer
    @DeleteMapping("/admin/customers/{id}")
    public ResponseEntity<?> deleteCustomer(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        customerService.deleteCustomer(id);

        // returns 204
        return ResponseEntity.noContent().build();
    }
}

