package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.account.http.request.BankingAccountCloseRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCreateRequest;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import net.datafaker.Faker;
import net.datafaker.providers.base.Country;
import net.datafaker.providers.base.Finance;
import net.datafaker.providers.base.Number;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankingAccountServiceTest {

    @Mock
    private BankingAccountRepository bankingAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Faker faker;

    @Mock
    private Finance finance;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private BankingAccountService bankingAccountService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    private final String rawPassword = "123456";

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customerA = new Customer(99L, "customerA@test.com", rawPassword);
        customerB = new Customer(92L, "customerB@test.com", rawPassword);
        customerAdmin = new Customer(95L, "admin@test.com", rawPassword);
        customerAdmin.setRole(CustomerRole.ADMIN);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should open a BankingAccount")
    void shouldCreateBankingAccount() {
        // given
        Country country = Mockito.mock(Country.class);
        Number number = Mockito.mock(Number.class);
        setUpContext(customerA);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";
        BankingAccountCreateRequest request = new BankingAccountCreateRequest(
                BankingAccountType.SAVINGS,
                BankingAccountCurrency.EUR
        );

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountCurrency(request.accountCurrency());
        bankingAccount.setAccountType(request.accountType());
        bankingAccount.setAccountNumber(accountNumber);

        // when
        Mockito.when(faker.country()).thenReturn(country);
        Mockito.when(faker.number()).thenReturn(number);
        when(faker.country().countryCode2()).thenReturn("US");
        when(customerRepository.findByEmail(customerA.getEmail())).thenReturn(Optional.of(customerA));
        when(bankingAccountRepository.save(any(BankingAccount.class))).thenReturn(bankingAccount);

        BankingAccount savedAccount = bankingAccountService.createBankingAccount(request);

        // then
        assertThat(savedAccount.getAccountCurrency()).isEqualTo(request.accountCurrency());
        assertThat(savedAccount.getAccountType()).isEqualTo(request.accountType());
        assertThat(savedAccount.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(savedAccount.getBalance()).isEqualTo(BigDecimal.valueOf(0));
        verify(bankingAccountRepository, times(1)).save(any(BankingAccount.class));
    }

    @Test
    @DisplayName("Should not open a BankingAccount when customer not found")
    void shouldNotCreateBankingAccountWhenCustomerNotFound() {
        // given
        setUpContext(customerA);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";
        BankingAccountCreateRequest request = new BankingAccountCreateRequest(
                BankingAccountType.SAVINGS,
                BankingAccountCurrency.EUR
        );

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountCurrency(request.accountCurrency());
        bankingAccount.setAccountType(request.accountType());
        bankingAccount.setAccountNumber(accountNumber);

        // when
        when(customerRepository.findByEmail(customerA.getEmail())).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> bankingAccountService.createBankingAccount(request)
        );

        // then
        assertTrue(exception.getMessage().contains("Customer not found"));
    }

    @Test
    @DisplayName("Should close a BankingAccount")
    void shouldCloseBankingAccount() {
        // given
        setUpContext(customerA);
        BankingAccountCloseRequest request = new BankingAccountCloseRequest(
                rawPassword
        );

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setId(5L);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountNumber(accountNumber);

        // when
        Mockito.when(bCryptPasswordEncoder.matches(rawPassword, customerA.getPassword())).thenReturn(true);
        when(bankingAccountRepository.findById(bankingAccount.getId())).thenReturn(Optional.of(bankingAccount));
        when(bankingAccountRepository.save(any(BankingAccount.class))).thenReturn(bankingAccount);

        BankingAccount savedAccount = bankingAccountService.closeBankingAccount(bankingAccount.getId(), request);

        // then
        assertThat(savedAccount.getAccountStatus()).isEqualTo(BankingAccountStatus.CLOSED);
        verify(bankingAccountRepository, times(1)).save(any(BankingAccount.class));
    }

    @Test
    @DisplayName("Should not close BankingAccount When none is found")
    void shouldNotCloseBankingAccountWhenNotFound() {
        // given
        setUpContext(customerA);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";
        BankingAccountCloseRequest request = new BankingAccountCloseRequest(
                rawPassword
        );

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountNumber(accountNumber);

        // when
        when(bankingAccountRepository.findById(bankingAccount.getId())).thenReturn(Optional.empty());

        BankingAccountNotFoundException exception = assertThrows(
                BankingAccountNotFoundException.class,
                () -> bankingAccountService.closeBankingAccount(bankingAccount.getId(), request)
        );

        // then
        assertTrue(exception.getMessage().contains("Banking account not found"));
    }

    @Test
    @DisplayName("Should not close account if you are not the owner and you are not admin either")
    void shouldNotCloseBankingAccountWhenItsNotYoursAndYouAreNotAdmin() {
        // given
        setUpContext(customerA);
        BankingAccountCloseRequest request = new BankingAccountCloseRequest(
                rawPassword
        );

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount bankingAccount = new BankingAccount(customerB);
        bankingAccount.setId(5L);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountNumber(accountNumber);

        // when
        when(bankingAccountRepository.findById(bankingAccount.getId())).thenReturn(Optional.of(bankingAccount));

        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingAccountService.closeBankingAccount(bankingAccount.getId(), request)
        );

        // then
        assertTrue(exception.getMessage().contains("You are not the owner of this account."));
    }

    @Test
    @DisplayName("Should close an account even if its not yours when you are ADMIN")
    void shouldCloseBankingAccountWhenItsNotYoursAndButYouAreAdmin() {
        // given
        setUpContext(customerAdmin);
        BankingAccountCloseRequest request = new BankingAccountCloseRequest(
                rawPassword
        );

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setId(5L);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountNumber(accountNumber);

        // when
        when(bankingAccountRepository.findById(bankingAccount.getId())).thenReturn(Optional.of(bankingAccount));
        when(bankingAccountRepository.save(any(BankingAccount.class))).thenReturn(bankingAccount);

        BankingAccount savedAccount = bankingAccountService.closeBankingAccount(bankingAccount.getId(), request);

        // then
        assertThat(savedAccount.getAccountStatus()).isEqualTo(BankingAccountStatus.CLOSED);
        verify(bankingAccountRepository, times(1)).save(any(BankingAccount.class));
    }

    @Test
    @DisplayName("Should get a customer with its banking account data")
    void shouldGetCustomerBankingAccountsFromCustomer() {
        // given
        Set<BankingAccount> bankingAccounts = new HashSet<>();
        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountNumber("US99 0000 1111 1122 3333 4444");
        bankingAccounts.add(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customerA);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountNumber("US99 0000 1111 1122 3333 6666");
        bankingAccounts.add(bankingAccountB);

        // when
        when(bankingAccountRepository.findByCustomer_Id(anyLong())).thenReturn(bankingAccounts);

        Set<BankingAccount> result = bankingAccountService.getCustomerBankingAccounts(customerA.getId());

        // then
        assertThat(result.size()).isEqualTo(2);
        verify(bankingAccountRepository, times(1)).findByCustomer_Id(anyLong());
    }

    //    @Test
    //    @DisplayName("Should create a transaction deposit")
    //    void shouldDeposit() {
    //        // given
    //        BankingAccount bankingAccount = new BankingAccount(customerA);
    //        bankingAccount.setId(12L);
    //        bankingAccount.setAccountNumber("ES1234567890123456789012");
    //        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccount.setBalance(BigDecimal.ZERO);
    //
    //        BankingTransaction givenTransaction = new BankingTransaction();
    //        givenTransaction.setTransactionType(BankingTransactionType.DEPOSIT);
    //        givenTransaction.setId(5L);
    //        givenTransaction.setAmount(BigDecimal.valueOf(200));
    //        givenTransaction.setDescription("Just a gift :)");
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                givenTransaction.getAmount(),
    //                givenTransaction.getTransactionType(),
    //                givenTransaction.getDescription()
    //        );
    //
    //        // when
    //        when(bankingAccountRepository.findById(bankingAccount.getId())).thenReturn(Optional.of(bankingAccount));
    //        BankingTransaction storedTransaction = bankingAccountService.handleCreateTransactionRequest(
    //                bankingAccount.getId(),
    //                request
    //        );
    //
    //        // then
    //        verify(bankingAccountRepository, times(1)).save(any(BankingAccount.class));
    //        assertThat(storedTransaction.getAmount()).isEqualTo(request.amount());
    //        assertThat(storedTransaction.getDescription()).isEqualTo(request.description());
    //        assertThat(storedTransaction.getTransactionType()).isEqualTo(request.transactionType());
    //    }

    //    @DisplayName("Should create a transfer transaction")
    //    void shouldTransferToAnotherCustomer() {
    //        // given
    //        setUpContext(customerA);
    //
    //        final long bankingAccountA_StartBalance = 1000;
    //        BankingAccount bankingAccountA = new BankingAccount(customerA);
    //        bankingAccountA.setAccountNumber("ES1234567890123444449013");
    //        bankingAccountA.setId(1L);
    //        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccountA.setBalance(BigDecimal.valueOf(bankingAccountA_StartBalance));
    //
    //        final long bankingAccountB_StartBalance = 0;
    //        BankingAccount bankingAccountB = new BankingAccount(customerB);
    //        bankingAccountB.setId(5L);
    //        bankingAccountB.setAccountNumber("ES1234567890123456789012");
    //        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccountB.setBalance(BigDecimal.valueOf(bankingAccountB_StartBalance));
    //
    //        BankingTransaction givenTransaction = new BankingTransaction();
    //        givenTransaction.setTransactionType(BankingTransactionType.TRANSFER_TO);
    //        givenTransaction.setId(5L);
    //        givenTransaction.setAmount(BigDecimal.valueOf(200));
    //        givenTransaction.setDescription("Just a gift :)");
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                bankingAccountB.getAccountNumber(),
    //                givenTransaction.getAmount(),
    //                givenTransaction.getTransactionType(),
    //                givenTransaction.getDescription()
    //        );
    //
    //        // when
    //        when(bankingAccountRepository.findById(bankingAccountB.getId())).thenReturn(Optional.of(bankingAccountB));
    //        when(bankingAccountRepository.save(bankingAccountB)).thenReturn(bankingAccountB);
    //        when(bankingAccountRepository.findById(bankingAccountA.getId())).thenReturn(Optional.of(bankingAccountA));
    //        when(bankingAccountRepository.save(bankingAccountA)).thenReturn(bankingAccountA);
    //        BankingTransaction storedTransaction = bankingAccountService.handleCreateTransactionRequest(
    //                bankingAccountA.getId(),
    //                request
    //        );
    //
    //        // then
    //        verify(bankingAccountRepository, times(2)).save(any(BankingAccount.class));
    //        assertThat(storedTransaction.getAmount()).isEqualTo(request.amount());
    //        assertThat(storedTransaction.getDescription()).isEqualTo(request.description());
    //        assertThat(storedTransaction.getTransactionType()).isEqualTo(request.transactionType());
    //        // banking account A should be 0
    //        assertThat(bankingAccountA.getBalance()).isEqualTo(
    //                BigDecimal.valueOf(bankingAccountA_StartBalance).subtract(request.amount())
    //        );
    //        // banking account b should be 200
    //        assertThat(bankingAccountB.getBalance()).isEqualTo(
    //                BigDecimal.valueOf(bankingAccountB_StartBalance).add(request.amount())
    //        );
    //
    //    }
    //
    //    @Test
    //    @DisplayName("Should not transfer to same banking account")
    //    void shouldNotTransferToSameBankingAccount() {
    //        // given
    //        final long bankingAccountA_StartBalance = 1000;
    //        BankingAccount bankingAccountA = new BankingAccount(customerA);
    //        bankingAccountA.setAccountNumber("ES1234567890123444449013");
    //        bankingAccountA.setId(1L);
    //        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccountA.setBalance(BigDecimal.valueOf(bankingAccountA_StartBalance));
    //        bankingAccountRepository.save(bankingAccountA);
    //
    //        BankingTransaction givenTransaction = new BankingTransaction();
    //        givenTransaction.setTransactionType(BankingTransactionType.TRANSFER_TO);
    //        givenTransaction.setId(5L);
    //        givenTransaction.setAmount(BigDecimal.valueOf(200));
    //        givenTransaction.setDescription("Just a gift :)");
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                bankingAccountA.getAccountNumber(),
    //                givenTransaction.getAmount(),
    //                givenTransaction.getTransactionType(),
    //                givenTransaction.getDescription()
    //        );
    //
    //        // when
    //        BankingAccountException exception = assertThrows(
    //                BankingAccountException.class,
    //                () -> bankingAccountService.handleCreateTransactionRequest(
    //                        bankingAccountA.getId(),
    //                        request
    //                )
    //        );
    //
    //        // then
    //        //        verify(bankingAccountRepository, times(0)).save(any(BankingAccount.class));
    //        assertThat(exception.getMessage()).isEqualTo(
    //                "You cannot transfer to the same banking account"
    //        );
    //    }
    //
    //    @Test
    //    @DisplayName("Should not transfer when account not exists and balance must remain same")
    //    void shouldNotTransferWhenDestinyNotExist() {
    //        // given
    //
    //        final long bankingAccountA_StartBalance = 1000;
    //        BankingAccount bankingAccountA = new BankingAccount(customerA);
    //        bankingAccountA.setAccountNumber("ES1234567890123444449013");
    //        bankingAccountA.setId(1L);
    //        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccountA.setBalance(BigDecimal.valueOf(bankingAccountA_StartBalance));
    //
    //        BankingTransaction givenTransaction = new BankingTransaction();
    //        givenTransaction.setTransactionType(BankingTransactionType.TRANSFER_TO);
    //        givenTransaction.setId(5L);
    //        givenTransaction.setAmount(BigDecimal.valueOf(200));
    //        givenTransaction.setDescription("Just a gift :)");
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                "FAKE ACCOUNT NUMBER",
    //                givenTransaction.getAmount(),
    //                givenTransaction.getTransactionType(),
    //                givenTransaction.getDescription()
    //        );
    //
    //        // when
    //        BankingAccountNotFoundException exception = assertThrows(
    //                BankingAccountNotFoundException.class,
    //                () -> bankingAccountService.handleCreateTransactionRequest(
    //                        bankingAccountA.getId(),
    //                        request
    //                )
    //        );
    //
    //        // then
    //        verify(bankingAccountRepository, times(0)).save(any(BankingAccount.class));
    //        assertThat(bankingAccountA.getBalance()).isEqualTo(BigDecimal.valueOf(bankingAccountA_StartBalance));
    //        assertTrue(exception.getMessage().contains("Banking account not found"));
    //    }
}
