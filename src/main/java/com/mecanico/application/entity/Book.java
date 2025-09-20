/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.entity;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Book {
    
    private LocalDate date;
    private String expInvNr;
    private String expDescription;
    private double expPrice;
    private double expBtwPercentage;
    private double expBtwPrice;
    private double expBtwValue;
    
    private Long invId;
    private String invClientName;
    private double invPriceIncBtw;
    private double invBtwPercentage;
    private double invPriceExBtw;
    private double invBtwValue;
    
}
