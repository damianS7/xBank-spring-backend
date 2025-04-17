package com.damian.xBank.auth;

import com.damian.xBank.customer.Customer;
import jakarta.persistence.*;

@Entity
@Table(name = "customer_auth")
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "auth_status")
    @Enumerated(EnumType.STRING)
    private AuthenticationStatus status;

    @Column(name = "password_hash")
    private String passwordHash;

    public Auth() {
        this.status = AuthenticationStatus.ENABLED_ACCOUNT;
        this.emailVerified = false;
    }

    public Auth(Customer customer) {
        this();
        this.customer = customer;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return this.customer.getId();
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public boolean isEmailVerified() {
        return this.emailVerified;
    }

    public AuthenticationStatus getStatus() {
        return this.status;
    }

    public void setStatus(AuthenticationStatus status) {
        this.status = status;
    }

}
