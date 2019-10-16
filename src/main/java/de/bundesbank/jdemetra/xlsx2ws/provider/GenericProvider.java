/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import java.util.HashMap;
import java.util.Map;

public class GenericProvider implements IProvider {

    public static final String SOURCE = "moniker",
            ID = "id";
    private final Map<String, String> informations = new HashMap<>();

    @Override
    public Ts readTs() {
        if (informations.containsKey(SOURCE) && informations.containsKey(ID)) {
            TsMoniker tsMoniker = new TsMoniker(informations.get(SOURCE), informations.get(ID));
            return TsFactory.instance.createTs(null, tsMoniker, TsInformationType.All);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String> writeTs(TsMoniker moniker) {
        informations.put(SOURCE, moniker.getSource());
        informations.put(ID, moniker.getId());
        return informations;
    }

    @Override
    public void putInformation(String key, String value) {
        informations.put(key, value);
    }

}
