/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import ec.tstoolkit.design.ServiceDefinition;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceDefinition
public interface ISpecificationReaderFactory<T extends ISpecificationReader> {

    String getSpecificationName();

    T getNewInstance();

}
