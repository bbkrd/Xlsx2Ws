/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import de.bundesbank.webservice.BubaWebProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceProvider(service = IProviderFactory.class)
public class ZISProviderFactory implements IProviderFactory<ZISProvider> {

    private static final String PROVIDER_NAME = "ZISDB";
    private static final String SOURCE_NAME = BubaWebProvider.SOURCE;

    @Override
    public ZISProvider getNewInstance() {
        return new ZISProvider();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

}
