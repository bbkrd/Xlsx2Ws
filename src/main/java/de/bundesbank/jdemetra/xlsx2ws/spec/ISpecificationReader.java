/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.SpecificationDTO;
import ec.satoolkit.ISaSpecification;
import java.util.Map;

/**
 *
 * @author Thomas Witthohn
 */
public interface ISpecificationReader<T extends ISaSpecification> {

    SpecificationDTO<T> readSpecification(ISaSpecification old);

    Map<String, String> writeSpecification(T spec);

    void putInformation(String key, String value);
}
