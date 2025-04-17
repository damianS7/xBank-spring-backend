package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class BankingAccountController {
    private final BankingAccountService bankingAccountService;

    @Autowired
    public BankingAccountController(BankingAccountService bankingAccountService) {
        this.bankingAccountService = bankingAccountService;
    }

    // endpoint to modify a BankingAccount
    @PutMapping("/banking_account/{id}")
    public ResponseEntity<?> updateBankingAccount(@Validated @RequestBody BankingAccountUpdateRequest request) {
        BankingAccount bankingAccount = bankingAccountService.updateBankingAccount(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccount.toDTO());
    }

    // endpoint to open a new BankingAccount
    @PostMapping("/banking_account/open")
    public ResponseEntity<?> openBankingAccount(@Validated @RequestBody BankingAccountOpenRequest request) {
        BankingAccount bankingAccount = bankingAccountService.openBankingAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingAccount.toDTO());
    }

    // endpoint to open a new BankingAccount
    @GetMapping("/banking_account/{id}/close")
    public ResponseEntity<?> closeBankingAccount(@PathVariable Long id) {
        BankingAccount bankingAccount = bankingAccountService.closeBankingAccount(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccount.toDTO());
    }
}

