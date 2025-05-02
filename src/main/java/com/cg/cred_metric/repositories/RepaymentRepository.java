package com.cg.cred_metric.repositories;


import com.cg.cred_metric.models.Repayment;
import com.cg.cred_metric.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByUser(User user);

    long countByUserAndRepaymentTypeAndRepaymentTypeIDAndRepaymentStatusAndPaymentDateBetween(
            User user,
            Repayment.RepaymentType repaymentType,
            Long repaymentTypeId,
            Repayment.RepaymentStatus status,
            LocalDate startDate,
            LocalDate endDate
    );
    List<Repayment> findByUserAndPaymentDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
