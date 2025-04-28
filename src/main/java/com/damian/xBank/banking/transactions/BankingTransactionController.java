package com.damian.xBank.banking.transactions;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class BankingTransactionController {
    private final BankingTransactionService bankingTransactionService;

    @Autowired
    public BankingTransactionController(BankingTransactionService bankingTransactionService) {
        this.bankingTransactionService = bankingTransactionService;
    }

    // endpoint to patch a transaction field
    @PatchMapping("/admin/banking/transactions/{id}")
    public ResponseEntity<?> patchTransaction(
            @PathVariable @NotNull(message = "This field cannot be null") @Positive
            Long id,
            @Validated @RequestBody
            BankingTransactionPatchRequest request
    ) {
        BankingTransaction bankingTransaction = bankingTransactionService.patchStatusTransaction(
                id,
                request
        );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(bankingTransaction.getBankingAccount().toDTO());
    }
}

