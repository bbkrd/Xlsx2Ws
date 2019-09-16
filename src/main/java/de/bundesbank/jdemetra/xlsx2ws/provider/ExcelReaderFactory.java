/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import ec.tss.tsproviders.spreadsheet.SpreadSheetProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceProvider(service = IProviderFactory.class)
public class ExcelReaderFactory implements IProviderFactory<ExcelReader> {

    private static final String PROVIDER_NAME = "EXCEL";
    private static final String SOURCE_NAME = SpreadSheetProvider.SOURCE;

    @Override
    public ExcelReader getNewInstance() {
        return new ExcelReader();
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
