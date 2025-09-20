/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.services;

import com.mecanico.application.entity.Client;
import com.mecanico.application.entity.Expense;
import com.mecanico.application.repository.ExpenseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExpenseService {
    
    private final ExpenseRepository repository;
    
    public List<Expense> findAll() {
        return repository.findAll();
    }
    
    
    public Page<Expense> list(Pageable pageable, Specification<Expense> filter) {
        return repository.findAll(filter, pageable);
    }
    
     public Optional<Expense> get(long id) {
        return repository.findById(id);
    }
    
     // Save or update a client
    public Expense save(Expense expense) {
        try {
            return repository.save(expense);
        } catch(Exception e) {
           throw new RuntimeException(e);
        }         
    }

    // Delete a client
    public void delete(Expense expense) {
        repository.delete(expense);
    }

    // Find a client by id
    public Optional<Expense> findById(Long id) {
        return repository.findById(id);
    }
    
    public Page<Expense> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<Expense> search(LocalDate startDate, LocalDate endDate) {
        return repository.getList(startDate, endDate);
    }
    
}
