/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceProvider(service = ISpecificationReaderFactory.class)
public class X13SpecificationReaderFactory implements ISpecificationReaderFactory<X13SpecificationReader> {

    private static final String SPECIFICATION_NAME = "X13";

    @Override
    public X13SpecificationReader getNewInstance() {
        return new X13SpecificationReader();
    }

    @Override
    public String getSpecificationName() {
        return SPECIFICATION_NAME;
    }

}
