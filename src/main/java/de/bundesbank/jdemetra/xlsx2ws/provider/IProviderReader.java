/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import ec.tss.Ts;

/**
 *
 * @author Thomas Witthohn
 */
public interface IProviderReader {

    Ts readTs();

    void putInformation(String key, String value);

}
