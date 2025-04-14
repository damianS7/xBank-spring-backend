package com.damian.xBank.banking.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class BankingAccountController {
    private final BankingAccountService bankingAccountService;

    @Autowired
    public BankingAccountController(BankingAccountService bankingAccountService) {
        this.bankingAccountService = bankingAccountService;
    }

    // endpoint to modify a profile
    @PutMapping("/banking_account/{id}")
    public ResponseEntity<?> updateBankingAccount(@Validated @RequestBody BankingAccountUpdateRequest request) {
        BankingAccount bankingAccount = bankingAccountService.updateBankingAccount(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccount.toDTO());
    }
}

