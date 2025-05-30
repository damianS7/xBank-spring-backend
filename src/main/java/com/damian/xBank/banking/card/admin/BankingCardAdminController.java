package com.damian.xBank.banking.card.admin;

import com.damian.xBank.banking.card.BankingCardService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/admin")
@RestController
public class BankingCardAdminController {
    private final BankingCardService bankingCardService;

    @Autowired
    public BankingCardAdminController(
            BankingCardService bankingCardService
    ) {
        this.bankingCardService = bankingCardService;
    }

    // block card, unlockcard, getTransactionsCard

    public ResponseEntity<?> getCard(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        return null;
    }

    public ResponseEntity<?> deleteCard(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        // actor (customer or admin)
        // service.deleteCard(id, actor)
        return null;
    }

    public ResponseEntity<?> updateCard(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        return null;
    }

    // endpoint for
    @GetMapping("/banking/card/{id}/cancel")
    public ResponseEntity<?> cancelCard(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        //        BankingCard bankingCard = bankingCardService.cancelCard(id);
        //        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);
        //
        //        return ResponseEntity
        //                .status(HttpStatus.OK)
        //                .body(bankingCardDTO);
        return null;
    }
}

