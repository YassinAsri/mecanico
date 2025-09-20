/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.services;

import com.mecanico.application.repository.ReferenceRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gebruiker
 */
@Service
public class ReferenceService {
    
    private final ReferenceRepository referenceRepository;
    
    public ReferenceService(ReferenceRepository referenceRepository) {
        this.referenceRepository = referenceRepository;
    }
    
    public List<String> getReferences() {
        return referenceRepository.getDistinctRefNames();
    }    
}
