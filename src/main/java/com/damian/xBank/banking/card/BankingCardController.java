package com.damian.xBank.banking.card;

import com.damian.xBank.auth.http.PasswordConfirmationRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionDTO;
import com.damian.xBank.banking.transactions.BankingTransactionDTOMapper;
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
    public ResponseEntity<?> customerGetBankingCards() {
        Set<BankingCard> bankingCards = bankingCardService.getBankingCards();
        Set<BankingCardDTO> bankingCardsDTO = BankingCardDTOMapper.toBankingCardSetDTO(bankingCards);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardsDTO);
    }

    // endpoint to create a transaction with a BankingCard
    @PostMapping("/customers/me/banking/cards/{id}/spend")
    public ResponseEntity<?> customerCardSpend(
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


    // endpoint for logged customer to cancel a BankingCard
    @PostMapping("/customers/me/banking/cards/{id}/cancel")
    public ResponseEntity<?> customerCancelBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            PasswordConfirmationRequest request
    ) {
        BankingCard bankingCard = bankingCardService.cancelCardRequest(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to set PIN on a BankingCard
    @PutMapping("/customers/me/banking/cards/{id}/pin")
    public ResponseEntity<?> customerSetBankingCardPin(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardSetPinRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setBankingCardPinRequest(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to set a daily limit
    @PutMapping("/customers/me/banking/cards/{id}/daily-limit")
    public ResponseEntity<?> customerSetBankingCardDailyLimit(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardSetDailyLimitRequest request
    ) {
        BankingCard bankingCard = bankingCardService.setDailyLimitRequest(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to lock or unlock a BankingCard
    @PutMapping("/customers/me/banking/cards/{id}/lock")
    public ResponseEntity<?> customerLockBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            PasswordConfirmationRequest request
    ) {
        BankingCard bankingCard = bankingCardService.lockCardRequest(
                id,
                request
        );
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to lock or unlock a BankingCard
    @PutMapping("/customers/me/banking/cards/{id}/unlock")
    public ResponseEntity<?> customerUnlockBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            PasswordConfirmationRequest request
    ) {
        BankingCard bankingCard = bankingCardService.unlockCardRequest(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingCardDTO);
    }

    // endpoint for logged customer to get all transactions of a BankingCard
    @GetMapping("/customers/me/banking/cards/{id}/transactions")
    public ResponseEntity<?> customerBankingCardTransactions(
            @PathVariable @NotNull @Positive
            Long id,
            @PageableDefault(size = 2, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<BankingTransaction> transactions = bankingCardService.getBankingCardTransactions(id, pageable);
        Page<BankingTransactionDTO> transactionDTOS = BankingTransactionDTOMapper
                .toBankingTransactionPageDTO(transactions);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(transactionDTOS);
    }
}