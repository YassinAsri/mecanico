/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.services;

import com.mecanico.application.entity.Client;
import com.mecanico.application.repository.ClientRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import java.util.Optional;
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
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    public List<Client> findAll() {
        return clientRepository.findAll();
    }
    
    
    public Page<Client> list(Pageable pageable, Specification<Client> filter) {
        return clientRepository.findAll(filter, pageable);
    }
    
     public Optional<Client> get(long id) {
        return clientRepository.findById(id);
    }
    
     // Save or update a client
    public Client save(Client client) {
        try {
            return clientRepository.save(client);
        } catch(Exception e) {
           return null; 
        }         
    }

    // Delete a client
    public void delete(Client client) {
        clientRepository.delete(client);
    }

    // Find a client by id
    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }
    
    public Page<Client> list(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    
}
