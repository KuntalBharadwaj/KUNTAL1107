package com.cg.cred_metric.repositories;

import com.cg.cred_metric.models.Repayment;
import com.cg.cred_metric.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByUser(User user);
}
