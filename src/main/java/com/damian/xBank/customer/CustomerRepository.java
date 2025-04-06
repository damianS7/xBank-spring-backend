package com.damian.xBank.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

//    @Modifying
//    @Query("UPDATE Customer u " +
//            "SET u.password =?2 WHERE u.id = ?1")
//    int updatePassword(Long id, String password);
}

