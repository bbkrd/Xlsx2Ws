/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import ec.satoolkit.x13.X13Specification;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceProvider(service = ISpecificationFactory.class)
public class X13SpecificationFactory implements ISpecificationFactory<X13SpecificationReader, X13SpecificationWriter> {

    private static final String SPECIFICATION_NAME = "X13";

    private static final Class<X13Specification> SUPPORTED_CLASS = X13Specification.class;

    @Override
    public X13SpecificationReader getNewReaderInstance() {
        return new X13SpecificationReader();
    }

    @Override
    public String getSpecificationName() {
        return SPECIFICATION_NAME;
    }

    @Override
    public String getSupportedClass() {
        return SUPPORTED_CLASS.getName();
    }

    @Override
    public X13SpecificationWriter getNewWriterInstance() {
        return new X13SpecificationWriter();
    }

}
