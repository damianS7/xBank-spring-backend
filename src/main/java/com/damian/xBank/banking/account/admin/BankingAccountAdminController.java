package com.damian.xBank.banking.account.admin;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.banking.account.BankingAccountDTOMapper;
import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.banking.account.http.request.BankingAccountAliasUpdateRequest;
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
public class BankingAccountAdminController {
    private final BankingAccountService bankingAccountService;

    @Autowired
    public BankingAccountAdminController(BankingAccountService bankingAccountService) {
        this.bankingAccountService = bankingAccountService;
    }

    // endpoint to receive all BankingAccounts from user
    @GetMapping("/admin/customers/{id}/banking/accounts")
    public ResponseEntity<?> getBankingAccounts(
            @PathVariable @Positive
            Long id
    ) {
        Set<BankingAccount> bankingAccounts = bankingAccountService
                .getCustomerBankingAccounts(id);

        Set<BankingAccountDTO> bankingAccountsDTO = BankingAccountDTOMapper
                .toBankingAccountSetDTO(bankingAccounts);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountsDTO);
    }

    // endpoint to set an alias for an account
    @PatchMapping("/admin/banking/accounts/{id}/alias")
    public ResponseEntity<?> setBankingAccountAlias(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            BankingAccountAliasUpdateRequest request
    ) {
        BankingAccount bankingAccount = bankingAccountService.setBankingAccountAlias(id, request.alias());
        BankingAccountDTO bankingAccountDTO = BankingAccountDTOMapper.toBankingAccountDTO(bankingAccount);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bankingAccountDTO);
    }

    // endpoint to close a BankingAccount
    @PatchMapping("/admin/banking/accounts/{id}/close")
    public ResponseEntity<?> customerCloseBankingAccount(
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

