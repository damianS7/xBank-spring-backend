package com.damian.xBank.auth;

import com.damian.xBank.customer.Customer;
import jakarta.persistence.*;

@Entity
@Table(name = "auth")
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @Column
    private boolean verified;

    @Column
    @Enumerated(EnumType.STRING)
    private AuthenticationStatus status;

    @Column(name = "password_hash")
    private String passwordHash;

    public Auth() {
    }

    public Auth(String passwordHash) {
        this.passwordHash = passwordHash;
        this.status = AuthenticationStatus.EXPIRED;
        this.verified = false;
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

    public boolean isVerified() {
        return this.verified;
    }

    public AuthenticationStatus getStatus() {
        return this.status;
    }

    public void setStatus(AuthenticationStatus status) {
        this.status = status;
    }

}
