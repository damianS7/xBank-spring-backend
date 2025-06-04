package com.damian.xBank.banking.card.admin;

import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardDTO;
import com.damian.xBank.banking.card.BankingCardDTOMapper;
import com.damian.xBank.banking.card.BankingCardService;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetLockStatusRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
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
public class BankingCardAdminController {
    private final BankingCardService bankingCardService;

    @Autowired
    public BankingCardAdminController(
            BankingCardService bankingCardService
    ) {
        this.bankingCardService = bankingCardService;
    }

    // endpoint to fetch all cards of a customer
    @GetMapping("/admin/customers/{id}/banking/cards")
    public ResponseEntity<?> getCustomerBankingCards(
            @PathVariable @Positive
            Long id
    ) {
        Set<BankingCard> bankingCards = bankingCardService.getCustomerBankingCards(id);
        Set<BankingCardDTO> bankingCardsDTO = BankingCardDTOMapper.toBankingCardSetDTO(bankingCards);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardsDTO);
    }

    // endpoint to cancel a BankingCard
    @PatchMapping("/admin/banking/cards/{id}/cancel")
    public ResponseEntity<?> customerCancelBankingCard(
            @PathVariable @Positive
            Long id
    ) {
        BankingCard bankingCard = bankingCardService.cancelCard(id);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for to set PIN on a BankingCard
    @PatchMapping("/admin/banking/cards/{id}/pin")
    public ResponseEntity<?> customerSetPinBankingCard(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            BankingCardSetPinRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setBankingCardPin(id, request.pin());
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint to set a daily limit
    @PatchMapping("/admin/banking/cards/{id}/daily-limit")
    public ResponseEntity<?> setDailyLimit(
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

    // endpoint to lock or unlock a BankingCard
    @PatchMapping("/admin/banking/cards/{id}/lock-status")
    public ResponseEntity<?> customerLockBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardSetLockStatusRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setCardLockStatus(
                id,
                request.lockStatus()
        );
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }
}

