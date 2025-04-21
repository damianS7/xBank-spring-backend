package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.account.transactions.BankingAccountTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankingAccountServiceTestIntegrationTest {

    @Autowired
    private BankingAccountRepository bankingAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankingAccountService bankingAccountService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customerA = new Customer("customerA@test.com", "123456");
        customerB = new Customer("customerB@test.com", "123456");
        customerAdmin = new Customer("admin@test.com", "123456");
        customerAdmin.setRole(CustomerRole.ADMIN);
    }

    @Test
    void shouldRollbackTransferIfReceiverAccountDoesNotExist() {
        // given
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customerA, null, Collections.emptyList()));

        BankingAccount senderAccount = new BankingAccount(customerA);
        senderAccount.setAccountNumber("US00 0000 1111 2222 3333 4444");
        senderAccount.setAccountType(BankingAccountType.SAVINGS);
        senderAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        senderAccount.setBalance(BigDecimal.valueOf(1000));
        customerRepository.save(customerA);

        BankingAccount receiverAccount = new BankingAccount(customerB);
        receiverAccount.setAccountNumber("US00 0000 1111 2222 3333 5555");
        receiverAccount.setAccountType(BankingAccountType.SAVINGS);
        receiverAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        receiverAccount.setBalance(BigDecimal.valueOf(1000));
        customerRepository.save(customerB);

        // when
        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                999L, // ID que no existe para forzar el fallo
                BigDecimal.valueOf(200),
                BankingAccountTransactionType.TRANSFER_TO,
                "Test Transfer"
        );

        assertThrows(
                BankingAccountException.class,
                () -> bankingAccountService.handleCreateTransactionRequest(senderAccount.getId(), request)
        );

        // then
        BankingAccount refreshedSenderAccount = bankingAccountRepository.findById(senderAccount.getId()).get();
        BankingAccount refreshedReceiverAccount = bankingAccountRepository.findById(receiverAccount.getId()).get();

        // Verificamos que el balance NO haya cambiado (rollback)
        assertThat(refreshedSenderAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(3));
        assertThat(refreshedReceiverAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(3));

        // Verificamos que no se haya guardado ninguna transacción
        assertThat(refreshedSenderAccount.getAccountTransactions()).isEmpty();
        assertThat(refreshedReceiverAccount.getAccountTransactions()).isEmpty();
    }
}
