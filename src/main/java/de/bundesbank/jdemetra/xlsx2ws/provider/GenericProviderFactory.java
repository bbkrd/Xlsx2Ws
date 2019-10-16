/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = IProviderFactory.class)
public class GenericProviderFactory implements IProviderFactory<GenericProvider> {

    public static final String NAME = "GENERIC";

    @Override
    public String getProviderName() {
        return NAME;
    }

    @Override
    public String getSourceName() {
        return NAME;
    }

    @Override
    public GenericProvider getNewInstance() {
        return new GenericProvider();
    }

}
