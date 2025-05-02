package com.cg.cred_metric.repositories;

import com.cg.cred_metric.models.CreditCard;
import com.cg.cred_metric.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    // Custom method to fetch all credit cards for a specific user
    List<CreditCard> findByUser(User user);
    void deleteByUser(User user);
}

