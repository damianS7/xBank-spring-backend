package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.*;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardDTO;
import com.damian.xBank.banking.card.BankingCardDTOMapper;
import com.damian.xBank.banking.card.http.BankingCardRequest;
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
public class BankingAccountController {
    private final BankingAccountService bankingAccountService;
    private final BankingAccountCardManagerService bankingAccountCardManagerService;

    @Autowired
    public BankingAccountController(
            BankingAccountService bankingAccountService,
            BankingAccountCardManagerService bankingAccountCardManagerService
    ) {
        this.bankingAccountService = bankingAccountService;
        this.bankingAccountCardManagerService = bankingAccountCardManagerService;
    }

    // endpoint to set an alias for an account
    @PutMapping("/customers/me/banking/account/{id}/alias")
    public ResponseEntity<?> putBankingAccountAlias(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountAliasUpdateRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.setBankingAccountAlias(id, request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint to receive accounts from logged customer
    @GetMapping("/customers/me/banking/accounts")
    public ResponseEntity<?> getLoggedCustomerBankingAccounts() {
        Set<BankingAccount> bankingAccounts = bankingAccountService.getCustomerLoggedBankingAccounts();
        Set<BankingAccountDTO> bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountSetDTO(bankingAccounts);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint to generate a transaction
    @PostMapping("/customers/me/banking/account/{id}/transfer-to")
    public ResponseEntity<?> customerTransferRequest(
            @PathVariable @NotNull(message = "This field cannot be null") @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountTransferRequest request
    ) {
        BankingTransaction bankingTransaction = bankingAccountService.transferRequest(
                id,
                request
        );

        BankingTransactionDTO bankingTransactionDTO = BankingTransactionDTOMapper
                .toBankingTransactionDTO(bankingTransaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingTransactionDTO);
    }

    // endpoint for logged customer to open a new BankingAccount
    @PostMapping("/customers/me/banking/accounts/open")
    public ResponseEntity<?> createBankingAccount(
            @Validated @RequestBody
            BankingAccountCreateRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.createBankingAccount(request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingAccountDTO);
    }

    // endpoint for logged customer to re-open an existing BankingAccount
    @PostMapping("/customers/me/banking/account/{id}/open")
    public ResponseEntity<?> loggedCustomerOpenBankingAccount(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountOpenRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.openBankingAccount(id, request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint for logged customer to close a BankingAccount
    @PostMapping("/customers/me/banking/account/{id}/close")
    public ResponseEntity<?> loggedCustomerCloseBankingAccount(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountCloseRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.closeBankingAccount(id, request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint for logged customer to request for a new BankingCard
    @PostMapping("/customers/me/banking/accounts/{id}/card/request")
    public ResponseEntity<?> customerRequestBankingCard(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingCardRequest request
    ) {
        BankingCard bankingCard = bankingAccountCardManagerService.requestBankingCard(id, request);
        BankingCardDTO bankingCardDTO = BankingCardDTOMapper.toBankingCardDTO(bankingCard);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingCardDTO);
    }
}

