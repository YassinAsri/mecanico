/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.repository;

import com.mecanico.application.entity.Invoice;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> { 
    
    @Query("select x from Invoice x where x.date >= :startDate and x.date <= :endDate")
    List<Invoice> getList(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
}
