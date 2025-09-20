/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.services;

import com.mecanico.application.data.SamplePerson;
import com.mecanico.application.entity.Invoice;
import com.mecanico.application.repository.InvoiceRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gebruiker
 */
@Service
@AllArgsConstructor
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    
    public Invoice save(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }
    
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("not found"));
    }
    
     public Page<Invoice> list(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    public Page<Invoice> list(Pageable pageable, Specification<Invoice> filter) {
        return invoiceRepository.findAll(filter, pageable);
    }

    public List<Invoice> search(LocalDate startDate, LocalDate endDate) {        
        return invoiceRepository.getList(startDate.atStartOfDay(), endDate.atStartOfDay());
    }
    
}
