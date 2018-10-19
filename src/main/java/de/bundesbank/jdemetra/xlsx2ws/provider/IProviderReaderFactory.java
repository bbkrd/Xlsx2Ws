/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import ec.tstoolkit.design.ServiceDefinition;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceDefinition
public interface IProviderReaderFactory<T extends IProviderReader> {

    String getProviderName();

    T getNewInstance();

}
