package com.damian.xBank.banking.card;

import com.damian.xBank.auth.http.PasswordConfirmationRequest;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetLockStatusRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
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
    public ResponseEntity<?> getCustomerBankingCards() {
        Set<BankingCard> bankingCards = bankingCardService.getCustomerBankingCards();
        Set<BankingCardDTO> bankingCardsDTO = BankingCardDTOMapper.toBankingCardSetDTO(bankingCards);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardsDTO);
    }

    // endpoint for logged customer to cancel a BankingCard
    @PatchMapping("/customers/me/banking/cards/{id}/cancel")
    public ResponseEntity<?> customerCancelBankingCard(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            PasswordConfirmationRequest request
    ) {
        BankingCard bankingCard = bankingCardService.cancelCard(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to set PIN on a BankingCard
    @PatchMapping("/customers/me/banking/cards/{id}/pin")
    public ResponseEntity<?> customerSetPinBankingCard(
            @PathVariable @Positive
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

    // endpoint for logged customer to set a daily limit
    @PatchMapping("/customers/me/banking/cards/{id}/daily-limit")
    public ResponseEntity<?> customerSetDailyLimitBankingCard(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            BankingCardSetDailyLimitRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setDailyLimit(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to lock or unlock a BankingCard
    @PatchMapping("/customers/me/banking/cards/{id}/lock-status")
    public ResponseEntity<?> customerLockBankingCard(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            BankingCardSetLockStatusRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setCardLockStatus(
                id,
                request
        );
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

}