package com.damian.xBank.banking.account.admin;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.banking.account.BankingAccountDTOMapper;
import com.damian.xBank.banking.account.BankingAccountService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

