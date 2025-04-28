package com.damian.xBank.customer;

import com.damian.xBank.auth.Auth;
import com.damian.xBank.auth.AuthenticationRepository;
import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountCurrency;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.account.BankingAccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthenticationRepository authRepository;

    @Autowired
    private BankingAccountRepository bankingAccountRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find a customer")
    void shouldFindCustomer() {
        // given
        final String customerEmail = "customer@test.com";
        final String customerPassword = "123456";
        Customer givenCustomer = new Customer(null, customerEmail, customerPassword);
        customerRepository.save(givenCustomer);

        // when
        Optional<Customer> optionalCustomer = customerRepository.findById(givenCustomer.getId());

        // then
        assertThat(optionalCustomer).isPresent();
        assertThat(optionalCustomer.get().getEmail()).isEqualTo(customerEmail);
        assertThat(optionalCustomer.get().getPassword()).isEqualTo(customerPassword);
    }

    @Test
    @DisplayName("Should not find a customer")
    void shouldNotFindCustomer() {
        // given
        Long customerId = -1L;

        // when
        boolean customerExists = customerRepository.existsById(customerId);

        // then
        assertThat(customerExists).isFalse();
    }

    @Test
    @DisplayName("Should save a customer")
    void shouldSaveCustomer() {
        // given
        final String customerEmail = "customer@test.com";
        final String customerPassword = "123456";

        // when
        Customer savedCustomer = customerRepository.save(
                new Customer(null, customerEmail, customerPassword)
        );

        // then
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getEmail()).isEqualTo(customerEmail);
        assertThat(savedCustomer.getPassword()).isEqualTo(customerPassword);
    }

    @Test
    @DisplayName("Should delete a customer")
    void shouldDeleteCustomerByIdCustomer() {
        // given
        final String customerEmail = "customer@test.com";
        final String customerPassword = "123456";

        Customer savedCustomer = customerRepository.save(
                new Customer(null, customerEmail, customerPassword)
        );

        // when
        customerRepository.deleteById(savedCustomer.getId());

        // then
        assertThat(customerRepository.existsById(savedCustomer.getId())).isFalse();
    }

    @Test
    @DisplayName("Should save customer with profile")
    void shouldSaveCustomerWithProfile() {
        // given
        final String customerName = "david";
        final String customerSurname = "white";
        final String customerPhone = "+11 664 563 521";
        final CustomerGender customerGender = CustomerGender.MALE;
        final LocalDate customerBirthdate = LocalDate.of(1989, 1, 1);
        final String customerCountry = "USA";
        final String customerAddress = "fake av, 44";
        final String customerPostal = "52342";
        final String customerNationalId = "444111222J";
        final String customerPhotoPath = "/upload/images/9sdf324283sdf47293479fsdff23232347.jpg";

        final Customer givenCustomer = new Customer(null, "customer@test.com", "123456");
        givenCustomer.getProfile().setName(customerName);
        givenCustomer.getProfile().setSurname(customerSurname);
        givenCustomer.getProfile().setPhone(customerPhone);
        givenCustomer.getProfile().setGender(customerGender);
        givenCustomer.getProfile().setBirthdate(customerBirthdate);
        givenCustomer.getProfile().setCountry(customerCountry);
        givenCustomer.getProfile().setAddress(customerAddress);
        givenCustomer.getProfile().setPostalCode(customerPostal);
        givenCustomer.getProfile().setNationalId(customerNationalId);
        givenCustomer.getProfile().setPhotoPath(customerPhotoPath);

        // when
        final Customer savedCustomer = customerRepository.save(givenCustomer);

        // then
        assertThat(customerRepository.existsById(savedCustomer.getId())).isTrue();
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getProfile().getName()).isEqualTo(customerName);
        assertThat(savedCustomer.getProfile().getSurname()).isEqualTo(customerSurname);
        assertThat(savedCustomer.getProfile().getPhone()).isEqualTo(customerPhone);
        assertThat(savedCustomer.getProfile().getGender()).isEqualTo(customerGender);
        assertThat(savedCustomer.getProfile().getBirthdate()).isEqualTo(customerBirthdate);
        assertThat(savedCustomer.getProfile().getCountry()).isEqualTo(customerCountry);
        assertThat(savedCustomer.getProfile().getAddress()).isEqualTo(customerAddress);
        assertThat(savedCustomer.getProfile().getPostalCode()).isEqualTo(customerPostal);
        assertThat(savedCustomer.getProfile().getNationalId()).isEqualTo(customerNationalId);
        assertThat(savedCustomer.getProfile().getPhotoPath()).isEqualTo(customerPhotoPath);
    }

    @Test
    @DisplayName("Should save customer with auth")
    void shouldSaveCustomerWithAuth() {
        // given
        Customer customer = customerRepository.save(
                new Customer(null, "customer@test.com", "123456")
        );

        // when
        Auth auth = authRepository.findByCustomer_Id(customer.getId()).orElseThrow();

        // then
        assertThat(customer.getId()).isNotNull();
        assertThat(customerRepository.existsById(customer.getId())).isTrue();
        assertThat(customer.getPassword()).isEqualTo(auth.getPassword());
        assertThat(auth).isNotNull();
        assertThat(auth.getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    @DisplayName("Should save customer with banking accounts")
    void shouldSaveCustomerWithBankingAccounts() {
        // given
        final String givenIban = "US00 0000 1111 2222 3333 4444";
        final Customer customer = customerRepository.save(
                new Customer(null, "customer@test.com", "123456")
        );

        final BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setAccountNumber(givenIban);
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setOwner(customer);
        customer.addBankingAccount(bankingAccount);


        // when
        final Customer savedCustomer = customerRepository.save(customer);
        final BankingAccount savedAccount = bankingAccountRepository.findByCustomer_Id(customer.getId())
                                                                    .iterator().next();

        // then
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getBankingAccounts().size()).isEqualTo(1);
        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAccountNumber()).isEqualTo(givenIban);
        assertThat(savedAccount.getAccountType()).isEqualTo(BankingAccountType.SAVINGS);
        assertThat(savedAccount.getAccountCurrency()).isEqualTo(BankingAccountCurrency.EUR);
        assertThat(savedAccount.getOwner().getId()).isEqualTo(savedCustomer.getId());
        assertThat(bankingAccountRepository.existsById(savedAccount.getId())).isTrue();
    }


}
