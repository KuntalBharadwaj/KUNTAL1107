package com.cg.cred_metric.repositories;

import com.cg.cred_metric.models.Loan;
import com.cg.cred_metric.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    long countByUserAndStartDateAfter(User user, LocalDate sinceDate);
    void deleteByUser(User user);
}
