package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.http.BankingCardCreateRequest;
import com.damian.xBank.banking.card.http.BankingCardLockStatusRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
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

import java.util.Set;

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

    // endpoint to fetch all cards of logged customer
    @GetMapping("/customers/me/banking/cards")
    public ResponseEntity<?> loggedCustomerGetBankingCards() {
        Set<BankingCard> bankingCards = bankingCardService.getBankingCards();
        Set<BankingCardDTO> bankingCardsDTO = BankingCardDTOMapper.toBankingCardSetDTO(bankingCards);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardsDTO);
    }

    // endpoint to create a transaction with a BankingCard
    @PostMapping("/customers/me/banking/card/{id}/spend")
    public ResponseEntity<?> loggedCustomerCardSpend(
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

    // endpoint for logged customer to create a new BankingCard
    @PostMapping("/customers/me/banking/account/{id}/cards")
    public ResponseEntity<?> loggedCustomerCreateBankingCard(
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

    // endpoint for logged customer to cancel a BankingCard
    @GetMapping("/customers/me/banking/card/{id}/cancel")
    public ResponseEntity<?> loggedCustomerCancelBankingCard(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        BankingCard bankingCard = bankingCardService.cancelCard(id);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to set PIN on a BankingCard
    @PutMapping("/customers/me/banking/cards/{id}/pin")
    public ResponseEntity<?> loggedCustomerSetBankingCardPin(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardSetPinRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setBankingCardPin(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to lock or unlock a BankingCard
    @PutMapping("/customers/me/banking/cards/{id}/locking")
    public ResponseEntity<?> loggedCustomerLockStatusBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardLockStatusRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setLockStatus(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

}

