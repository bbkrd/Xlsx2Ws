/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Data
public class RegressorInfo implements IProviderInfo {

    private String documentName;
    private String name;
    private String providerName;
    private final Map<String, String> providerInfos = new HashMap<>();

    public void addProviderInfo(String key, String value) {
        providerInfos.put(key, value.trim());
    }

    public boolean isValid() {
        return documentName != null && !documentName.isEmpty();
    }
}
