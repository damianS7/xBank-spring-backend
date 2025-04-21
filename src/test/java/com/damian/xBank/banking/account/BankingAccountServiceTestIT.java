package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.account.transactions.BankingAccountTransaction;
import com.damian.xBank.banking.account.transactions.BankingAccountTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class BankingAccountServiceTestIT {

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
    @DisplayName("Should not transfer anything")
    void shouldNotTransferAndRollback() {
        // given
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customerA, null, Collections.emptyList()));

        final long bankingAccountA_StartBalance = 1000;
        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountNumber("ES1234567890123444449013");
        bankingAccountA.setId(1L);
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountA.setBalance(BigDecimal.valueOf(bankingAccountA_StartBalance));

        final long bankingAccountB_StartBalance = 0;
        BankingAccount bankingAccountB = new BankingAccount(customerB);
        bankingAccountB.setId(5L);
        bankingAccountB.setAccountNumber("ES1234567890123456789012");
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountB.setBalance(BigDecimal.valueOf(bankingAccountB_StartBalance));

        BankingAccountTransaction givenTransaction = new BankingAccountTransaction();
        givenTransaction.setTransactionType(BankingAccountTransactionType.TRANSFER_TO);
        givenTransaction.setId(5L);
        givenTransaction.setAmount(BigDecimal.valueOf(200));
        givenTransaction.setDescription("Just a gift :)");

        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                bankingAccountB.getId(),
                givenTransaction.getAmount(),
                givenTransaction.getTransactionType(),
                givenTransaction.getDescription()
        );

        // when
        when(bankingAccountRepository.findById(bankingAccountB.getId())).thenReturn(Optional.of(bankingAccountB));
        when(bankingAccountRepository.save(bankingAccountB)).thenReturn(bankingAccountB);
        when(bankingAccountRepository.findById(bankingAccountA.getId())).thenReturn(Optional.of(bankingAccountA));
        when(bankingAccountRepository.save(bankingAccountA)).thenReturn(bankingAccountA);
        BankingAccountTransaction storedTransaction = bankingAccountService.handleCreateTransactionRequest(
                bankingAccountA.getId(),
                request
        );

        // then
        verify(bankingAccountRepository, times(2)).save(any(BankingAccount.class));
        assertThat(storedTransaction.getAmount()).isEqualTo(request.amount());
        assertThat(storedTransaction.getDescription()).isEqualTo(request.description());
        assertThat(storedTransaction.getTransactionType()).isEqualTo(request.transactionType());
        // banking account A should be 0
        assertThat(bankingAccountA.getBalance()).isEqualTo(
                BigDecimal.valueOf(bankingAccountA_StartBalance).subtract(request.amount())
        );
        // banking account b should be 200
        assertThat(bankingAccountB.getBalance()).isEqualTo(
                BigDecimal.valueOf(bankingAccountB_StartBalance).add(request.amount())
        );
    }

    @Test
    void shouldRollbackTransferIfReceiverAccountDoesNotExist() {
        // given
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customerA, null, Collections.emptyList()));

        customerRepository.save(customerA);

        BankingAccount senderAccount = new BankingAccount(customerA);
        senderAccount.setAccountNumber("SENDER-123");
        senderAccount.setAccountType(BankingAccountType.SAVINGS);
        senderAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        senderAccount.setBalance(BigDecimal.valueOf(1000));
        bankingAccountRepository.save(senderAccount);

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

        // Verificamos que el balance NO haya cambiado (rollback)
        assertThat(refreshedSenderAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000));

        // Verificamos que no se haya guardado ninguna transacción
        assertThat(refreshedSenderAccount.getAccountTransactions()).isEmpty();
    }
}
