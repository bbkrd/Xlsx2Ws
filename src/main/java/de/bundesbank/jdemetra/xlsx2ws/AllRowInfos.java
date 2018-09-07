/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Data
public class AllRowInfos {

    private String multidocName;
    private String saItemName;
    private String providerName;
    private final Map<String, String> providerInfos = new HashMap<>();
    private String specificationName;
    private final Map<String, String> specificationInfos = new HashMap<>();
    private final Map<String, String> metaData = new HashMap<>();

    public void addProviderInfo(String key, String value) {
        providerInfos.put(key, value);
    }

    public void addSpecificationInfos(String key, String value) {
        specificationInfos.put(key, value);
    }

    public void addMetaData(String key, String value) {
        metaData.put(key, value);
    }

    public boolean isValid() {
        return multidocName != null && saItemName != null && !multidocName.isEmpty() && !saItemName.isEmpty();
    }
}
