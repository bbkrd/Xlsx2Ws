/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = IProviderFactory.class)
public class PureDataProviderFactory implements IProviderFactory<PureDataProvider> {

    public static final String NAME = "DATA";

    @Override
    public String getProviderName() {
        return NAME;
    }

    @Override
    public String getSourceName() {
        return NAME;
    }

    @Override
    public PureDataProvider getNewInstance() {
        return new PureDataProvider();
    }

}
