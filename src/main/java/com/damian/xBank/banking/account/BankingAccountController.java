package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
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

    @Autowired
    public BankingAccountController(BankingAccountService bankingAccountService) {
        this.bankingAccountService = bankingAccountService;
    }

    // endpoint to receive accounts from logged customer
    @GetMapping("/banking/accounts/me")
    public ResponseEntity<?> getLoggedCustomerBankingAccounts() {
        Set<BankingAccount> bankingAccounts = bankingAccountService.getCustomerLoggedBankingAccounts();
        Set<BankingAccountDTO> bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountSetDTO(bankingAccounts);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint to generate a transaction
    @PostMapping("/banking/accounts/{id}/transactions")
    public ResponseEntity<?> createTransaction(
            @PathVariable @NotNull(message = "This field cannot be null") @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountTransactionCreateRequest request
    ) {
        BankingTransaction bankingTransaction = bankingAccountService.handleCreateTransactionRequest(
                id,
                request
        );

        BankingTransactionDTO bankingTransactionDTO = BankingTransactionDTOMapper
                .toBankingTransactionDTO(bankingTransaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingTransactionDTO);
    }

    // endpoint to open a new BankingAccount
    @PostMapping("/banking/accounts/open")
    public ResponseEntity<?> openBankingAccount(
            @Validated @RequestBody
            BankingAccountOpenRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.openBankingAccount(request);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankingAccountDTO);
    }

    // endpoint to close a BankingAccount
    @GetMapping("/banking/accounts/{id}/close")
    public ResponseEntity<?> closeBankingAccount(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        BankingAccount bankingAccount = bankingAccountService.closeBankingAccount(id);
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }
}

