/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.reader;

import ec.tss.Ts;
import ec.tstoolkit.design.ServiceDefinition;

/**
 *
 * @author Thomas Witthohn
 */
@ServiceDefinition
public interface IProviderReader {

    String getProviderName();

    Ts loadTs();

    void putInformation(String key, String value);

}
