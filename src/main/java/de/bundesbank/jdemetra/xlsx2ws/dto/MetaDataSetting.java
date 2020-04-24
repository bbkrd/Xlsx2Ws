/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.dto;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author s4504tw
 */
public class MetaDataSetting implements ISetting {

    public static final String META_DATA_SETTING = "meta.data.setting";

    private final Set<String> set;

    public MetaDataSetting(Set<String> set) {
        this.set = new HashSet<>(set);
    }

    public boolean contains(String in) {
        return set.contains(in);
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

}
