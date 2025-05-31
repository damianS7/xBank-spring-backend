package com.damian.xBank.banking.transactions;

import com.damian.xBank.banking.transactions.http.BankingAccountTransactionRequest;
import com.damian.xBank.banking.transactions.http.BankingCardTransactionRequest;
import jakarta.validation.constraints.NotNull;
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
public class BankingTransactionController {
    private final BankingTransactionService bankingTransactionService;

    @Autowired
    public BankingTransactionController(BankingTransactionService bankingTransactionService) {
        this.bankingTransactionService = bankingTransactionService;
    }

    // endpoint for logged customer to get all transactions of a BankingCard
    @GetMapping("/customers/me/banking/cards/{id}/transactions")
    public ResponseEntity<?> customerBankingCardTransactions(
            @PathVariable @NotNull @Positive
            Long id,
            @PageableDefault(size = 2, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<BankingTransaction> transactions = bankingTransactionService.getBankingCardTransactions(id, pageable);
        Page<BankingTransactionDTO> transactionDTOS = BankingTransactionDTOMapper
                .toBankingTransactionPageDTO(transactions);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(transactionDTOS);
    }

    // endpoint for logged customer to get all transactions of a BankingAccount
    @GetMapping("/customers/me/banking/accounts/{id}/transactions")
    public ResponseEntity<?> customerBankingAccountTransactions(
            @PathVariable @NotNull @Positive
            Long id,
            @PageableDefault(size = 2, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<BankingTransaction> transactions = bankingTransactionService.getBankingAccountTransactions(id, pageable);
        Page<BankingTransactionDTO> transactionDTOS = BankingTransactionDTOMapper
                .toBankingTransactionPageDTO(transactions);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(transactionDTOS);
    }

    // endpoint for logged customer to do card transactions
    @PostMapping("/customers/me/banking/cards/{id}/transactions")
    public ResponseEntity<?> customerBankingCardTransaction(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardTransactionRequest request
    ) {
        return null;
    }

    // endpoint for logged customer to do card transactions
    @PostMapping("/customers/me/banking/accounts/{id}/transactions")
    public ResponseEntity<?> customerBankingAccountTransaction(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountTransactionRequest request
    ) {
        return null;
    }
}

