package com.damian.xBank.banking.card;

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
public class BankingCardController {
    private final BankingCardService bankingCardService;

    @Autowired
    public BankingCardController(
            BankingCardService bankingCardService
    ) {
        this.bankingCardService = bankingCardService;
    }

    // endpoint to request a BankingCard
    @PostMapping("/banking/cards/{id}/spend")
    public ResponseEntity<?> spend(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountTransactionCreateRequest request
    ) {
        BankingAccountTransaction transaction = bankingCardService.spend(id, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction.toDTO());
    }

    // endpoint to request a BankingCard
    @PostMapping("/banking/accounts/{id}/cards")
    public ResponseEntity<?> requestBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardOpenRequest request
    ) {
        BankingCard bankingCard = bankingCardService.requestCard(id, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingCard.toDTO());
    }

    // endpoint to cancel a BankingCard
    @GetMapping("/banking/cards/{id}/cancel")
    public ResponseEntity<?> cancelBankingCard(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        BankingCard bankingCard = bankingCardService.cancelCard(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCard.toDTO());
    }
}

