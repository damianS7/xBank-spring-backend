package com.damian.xBank.banking.account.admin;

import com.damian.xBank.banking.account.BankingAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class BankingAccountAdminController {
    private final BankingAccountService bankingAccountService;

    @Autowired
    public BankingAccountAdminController(BankingAccountService bankingAccountService) {
        this.bankingAccountService = bankingAccountService;
    }

}

