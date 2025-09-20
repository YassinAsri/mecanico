
package com.mecanico.application.repository;

import com.mecanico.application.entity.Expense;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    
    @Query("select x from Expense x where x.date >= :startDate and x.date <= :endDate")
    List<Expense> getList(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
