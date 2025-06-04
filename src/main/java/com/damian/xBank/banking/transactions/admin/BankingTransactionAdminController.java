package com.damian.xBank.banking.transactions.admin;

import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionDTO;
import com.damian.xBank.banking.transactions.BankingTransactionDTOMapper;
import com.damian.xBank.banking.transactions.BankingTransactionService;
import com.damian.xBank.banking.transactions.http.BankingTransactionUpdateStatusRequest;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class BankingTransactionAdminController {
    private final BankingTransactionService bankingTransactionService;

    @Autowired
    public BankingTransactionAdminController(BankingTransactionService bankingTransactionService) {
        this.bankingTransactionService = bankingTransactionService;
    }

    //     endpoint to patch a transaction field
    @PatchMapping("/admin/banking/transactions/{id}")
    public ResponseEntity<?> patchTransaction(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            BankingTransactionUpdateStatusRequest request
    ) {
        BankingTransaction bankingTransaction = bankingTransactionService.updateTransactionStatus(
                id,
                request
        );

        BankingTransactionDTO bankingTransactionDTO = BankingTransactionDTOMapper
                .toBankingTransactionDTO(bankingTransaction);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(bankingTransactionDTO);
    }
}

