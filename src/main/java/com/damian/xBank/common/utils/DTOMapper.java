package com.damian.xBank.common.utils;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountDTO;

import java.util.Set;
import java.util.stream.Collectors;

public class DTOMapper {
    public static Set<BankingAccountDTO> map(Set<BankingAccount> bankingAccounts) {
        return bankingAccounts.stream().map(
                BankingAccount::toDTO
        ).collect(Collectors.toSet());
    }
}
