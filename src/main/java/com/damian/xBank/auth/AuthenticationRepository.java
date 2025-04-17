package com.damian.xBank.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticationRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByCustomer_Id(Long customerId);
}

