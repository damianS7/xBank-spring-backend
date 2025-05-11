package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.http.BankingCardCreateRequest;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionDTO;
import com.damian.xBank.banking.transactions.BankingTransactionDTOMapper;
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

    // endpoint to create a transaction with a BankingCard
    @PostMapping("/banking/cards/{id}/spend")
    public ResponseEntity<?> spend(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountTransactionCreateRequest request
    ) {
        BankingTransaction transaction = bankingCardService.spend(id, request);
        BankingTransactionDTO transactionDTO = BankingTransactionDTOMapper.toBankingTransactionDTO(transaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionDTO);
    }

    // endpoint to create a new BankingCard
    @PostMapping("/banking/accounts/{id}/cards")
    public ResponseEntity<?> createBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardCreateRequest request
    ) {
        BankingCard bankingCard = bankingCardService.createCard(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingCardDTO);
    }

    // endpoint to cancel a BankingCard
    @GetMapping("/banking/cards/{id}/cancel")
    public ResponseEntity<?> cancelBankingCard(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        BankingCard bankingCard = bankingCardService.cancelCard(id);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }
}

