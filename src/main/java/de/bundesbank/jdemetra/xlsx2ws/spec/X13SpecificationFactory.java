/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.X13WizardIterator;
import de.bundesbank.jdemetra.xlsx2ws.wizard.IChoose;
import ec.satoolkit.x13.X13Specification;
import org.openide.WizardDescriptor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceProviders(value = {
    @ServiceProvider(service = ISpecificationFactory.class),
    @ServiceProvider(service = IChoose.class)}
)
public class X13SpecificationFactory implements ISpecificationFactory<X13SpecificationReader, X13SpecificationWriter>, IChoose {

    private static final String SPECIFICATION_NAME = "X13";

    public static final Class<X13Specification> SUPPORTED_CLASS = X13Specification.class;

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

    @Override
    public WizardDescriptor.Iterator<WizardDescriptor> createIterator() {
        return new X13WizardIterator();
    }

}
