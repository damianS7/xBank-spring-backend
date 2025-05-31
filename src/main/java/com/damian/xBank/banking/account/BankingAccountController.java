package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountAliasUpdateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCloseRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCreateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardDTO;
import com.damian.xBank.banking.card.BankingCardDTOMapper;
import com.damian.xBank.banking.card.http.BankingCardRequest;
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

    // return all the accounts from the logged customer
    @GetMapping("/customers/me/banking/accounts")
    public ResponseEntity<?> getCustomerBankingAccounts() {
        Set<BankingAccount> bankingAccounts = bankingAccountService.getLoggedCustomerBankingAccounts();
        Set<BankingAccountDTO> bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountSetDTO(bankingAccounts);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint for logged customer to request for a new BankingAccount
    @PostMapping("/customers/me/banking/accounts/request")
    public ResponseEntity<?> requestBankingAccount(
            @Validated @RequestBody
            BankingAccountCreateRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.createBankingAccountForLoggedCustomer(request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingAccountDTO);
    }

    // endpoint for logged customer to re-open an existing BankingAccount
    @PatchMapping("/customers/me/banking/accounts/{id}/open")
    public ResponseEntity<?> customerOpenBankingAccount(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountOpenRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.openBankingAccountForLoggedCustomer(id, request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint for logged customer to close a BankingAccount
    @PatchMapping("/customers/me/banking/accounts/{id}/close")
    public ResponseEntity<?> customerCloseBankingAccount(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountCloseRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.closeBankingAccountForLoggedCustomer(id, request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint to set an alias for an account
    @PatchMapping("/customers/me/banking/accounts/{id}/alias")
    public ResponseEntity<?> setBankingAccountAlias(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountAliasUpdateRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.setBankingAccountAliasForLoggedCustomer(id, request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint for logged customer to request for a new BankingCard
    @PostMapping("/customers/me/banking/accounts/{id}/cards/request")
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

