/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import ec.tss.Ts;
import ec.tss.TsMoniker;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Thomas Witthohn
 */
public interface IProvider {

    Optional<Ts> readTs();

    Map<String, String> writeTs(TsMoniker moniker);

    void putInformation(String key, String value);

}
