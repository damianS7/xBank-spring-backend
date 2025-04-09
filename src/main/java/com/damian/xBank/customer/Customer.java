package com.damian.xBank.customer;

import com.damian.xBank.auth.Auth;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "customers")
public class Customer implements CustomerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private Auth auth;

    @Enumerated(EnumType.STRING)
    private CustomerRole role;

    public Customer() {
        this.auth = new Auth();
        this.auth.setCustomer(this);
    }

    public Customer(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.role = CustomerRole.CUSTOMER;
        this.auth = new Auth(password);
        this.auth.setCustomer(this);
    }

    public Customer(String email, String password) {
        this(null, email, password);
    }

    public void setAuth(Auth auth){
        this.auth = auth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.auth.setPassword(password);;
    }

    public CustomerRole getRole() {
        return role;
    }

    public void setRole(CustomerRole role) {
        this.role = role;
    }

    public CustomerDTO toDTO() {
        return CustomerDTO.build(this);
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(CustomerRole.CUSTOMER.name());
        return Collections.singletonList(authority);
    }
}
