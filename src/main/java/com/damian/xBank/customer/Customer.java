package com.damian.xBank.customer;

import com.damian.xBank.auth.Auth;
import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.common.utils.DTOMapper;
import com.damian.xBank.customer.profile.Profile;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
public class Customer implements CustomerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL) // FetchType EAGER por defecto
    private Auth auth;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL) // FetchType EAGER por defecto
    private Profile profile;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL) // FetchType LAZY por defecto
    private Set<BankingAccount> bankingAccounts;

    @Enumerated(EnumType.STRING)
    private CustomerRole role;

    public Customer() {
        this.auth = new Auth(this);
        this.profile = new Profile(this);
        this.role = CustomerRole.CUSTOMER;
        this.bankingAccounts = new HashSet<>();
    }

    public Customer(Long id, String email, String password) {
        this();
        this.id = id;
        this.email = email;
        this.setPassword(password);
    }

    public Customer(String email, String password) {
        this(null, email, password);
    }

    public Auth getAuth() {
        return this.auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.auth.setPassword(password);
    }

    public CustomerRole getRole() {
        return role;
    }

    public void setRole(CustomerRole role) {
        this.role = role;
    }

    public CustomerDTO toDTO() {
        return DTOMapper.build(this);
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return auth.getPassword();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return CustomerDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return CustomerDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return CustomerDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return CustomerDetails.super.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + this.role.name());
        return Collections.singletonList(authority);
    }

    public Profile getProfile() {
        return profile;
    }

    public String getFullName() {
        return getProfile().getName() + " " + getProfile().getSurname();
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Set<BankingAccount> getBankingAccounts() {
        return bankingAccounts;
    }

    public void addBankingAccount(BankingAccount account) {
        this.bankingAccounts.add(account);
    }

    public void setBankingAccounts(Set<BankingAccount> bankingAccounts) {
        this.bankingAccounts = bankingAccounts;
    }

}
