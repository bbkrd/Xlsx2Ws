/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceProvider(service = IProviderReaderFactory.class)
public class ZISReaderFactory implements IProviderReaderFactory<ZISReader> {

    private static final String PROVIDER_NAME = "ZISDB";

    @Override
    public ZISReader getNewInstance() {
        return new ZISReader();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

}
