package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.account.transactions.BankingAccountTransaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    // endpoint to generate a transactions for of certain account
    @PostMapping("/banking/accounts/{id}/transactions")
    public ResponseEntity<?> createTransaction(
            @PathVariable @NotNull(message = "This field cannot be null") @Positive Long id,
            @Validated @RequestBody BankingAccountTransactionCreateRequest request) {
        BankingAccountTransaction bankingAccountTransaction = bankingAccountService.handleCreateTransactionRequest(id, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingAccountTransaction.getOwnerAccount().toDTO());
    }

    // endpoint to open a new BankingAccount
    @PostMapping("/banking/accounts/open")
    public ResponseEntity<?> openBankingAccount(
            @Validated @RequestBody
            BankingAccountOpenRequest request) {
        BankingAccount bankingAccount = bankingAccountService.openBankingAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingAccount.toDTO());
    }

    // endpoint to close a BankingAccount
    @GetMapping("/banking/accounts/{id}/close")
    public ResponseEntity<?> closeBankingAccount(
            @PathVariable @NotNull @Positive
            Long id) {
        BankingAccount bankingAccount = bankingAccountService.closeBankingAccount(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccount.toDTO());
    }
}

