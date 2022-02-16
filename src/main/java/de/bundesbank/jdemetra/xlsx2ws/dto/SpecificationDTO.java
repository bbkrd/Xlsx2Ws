/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

import ec.satoolkit.ISaSpecification;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Value
public class SpecificationDTO<T extends ISaSpecification> {

    private final T specification;
    private final Message[] messages;
}
